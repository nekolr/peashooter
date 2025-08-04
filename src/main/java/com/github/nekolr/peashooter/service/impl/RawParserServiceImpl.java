package com.github.nekolr.peashooter.service.impl;

import com.alibaba.fastjson2.JSON;
import com.github.nekolr.peashooter.api.sonarr.SonarrV3Api;
import com.github.nekolr.peashooter.api.sonarr.rsp.Series;
import com.github.nekolr.peashooter.api.themoviedb.TheMovieDbApi;
import com.github.nekolr.peashooter.api.themoviedb.rsp.FindAliasTitle;
import com.github.nekolr.peashooter.api.themoviedb.rsp.FindById;
import com.github.nekolr.peashooter.config.SettingsManager;
import com.github.nekolr.peashooter.exception.ParseTitleException;
import com.github.nekolr.peashooter.parser.RawParser;
import com.github.nekolr.peashooter.rss.Enclosure;
import com.github.nekolr.peashooter.rss.Item;
import com.github.nekolr.peashooter.rss.convertor.RssConvertor;
import com.github.nekolr.peashooter.rss.loader.RssLoader;
import com.github.nekolr.peashooter.rss.writer.RssWriter;
import com.github.nekolr.peashooter.service.IRawParserService;
import com.github.nekolr.peashooter.util.*;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import jodd.io.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.github.nekolr.peashooter.constant.Peashooter.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class RawParserServiceImpl implements IRawParserService {

    private static final String DEFAULT_LANGUAGE = "Chinese";
    private static final String DEFAULT_QUALITY = "WEBDL-1080p";
    private static final List<String> EXCLUDE_SOURCES = List.of("CR", "BILIBILI");
    private static final long DEFAULT_VALID_SECONDS = 432000; // 5 天

    private static final String SONARR_ID_ALIAS_TITLE_CACHED_FILE_PATH = HOME_DIR + File.separator + "sonarr-id-alias-title-cached.json";

    private String automatedRssFileSignature = "0";

    private final Map<String, List<FindAliasTitle.Title>> sonarrIdTitleMap = new ConcurrentHashMap<>();

    private final RssLoader rssLoader;
    private final RssWriter rssWriter;
    private final SonarrV3Api sonarrV3Api;
    private final RssConvertor rssConvertor;
    private final TheMovieDbApi theMovieDbApi;
    private final SettingsManager settingsManager;
    private final ResolvePubDateUtil resolvePubDateUtil;

    /**
     * 自动解析数据源中的剧集信息
     */
    @Override
    public void autoParse(Long datasourceId) {
        List<Series> monitoredSeries = this.getMonitoredSeries();
        this.cachedSonarrIdTitleMap(monitoredSeries);

        String rss = rssLoader.loadFromFile(getDatasourceRssFilepath(datasourceId));
        SyndFeed syndFeed = FeedUtils.getFeed(rss);
        List<SyndEntry> entryList = FeedUtils.getEntries(syndFeed);
        List<Item> items = new ArrayList<>();
        if (!CollectionUtils.isEmpty(entryList)) {
            for (SyndEntry entry : entryList) {
                String title = FeedUtils.getTitle(entry);
                try {
                    log.info("开始解析种子标题: {}", title);
                    RawParser.Episode episode = RawParser.parse(title);
                    if (Objects.nonNull(episode.source())) {
                        if (EXCLUDE_SOURCES.contains(episode.source().toUpperCase())) {
                            log.info("跳过 {} 源，通过种子标题解析出的信息为: {}", String.join(",", EXCLUDE_SOURCES), episode);
                            continue;
                        }
                    }
                    String seriesId = null;
                    for (Series series : monitoredSeries) {
                        Optional<Item> optionalItem = this.getItem(episode, series, entry);
                        if (optionalItem.isPresent()) {
                            seriesId = series.id();
                            items.add(optionalItem.get());
                        }
                    }

                    if (Objects.isNull(seriesId)) {
                        log.warn("未匹配到剧集，通过种子标题解析出的信息为: {}", episode);
                    }

                } catch (ParseTitleException e) {
                    log.error("解析标题失败: {}", title);
                }
            }

            String xml = rssConvertor.combine(items, null);
            String signature = Md5Util.md5(xml);

            synchronized (this) {
                if (!signature.equals(automatedRssFileSignature)) {
                    automatedRssFileSignature = signature;
                    rssWriter.write(xml, getAutomatedGroupRssFilepath());
                }
            }
        }
    }

    private Optional<Item> getItem(RawParser.Episode episode, Series series, SyndEntry entry) {

        if (this.equalsTitle(episode.titleInfo().name(), series.title())) {
            log.info("直接匹配剧集: {}, 通过种子标题解析出的信息为: {}", series.title(), episode);
            return Optional.of(this.parseEntry(entry, series, episode));
        }

        if (this.similarTitle(episode.titleInfo().name(), series.title())) {
            log.info("模糊匹配剧集: {}, 通过种子标题解析出的信息为: {}", series.title(), episode);
            return Optional.of(this.parseEntry(entry, series, episode));
        }

        List<FindAliasTitle.Title> titleList = this.sonarrIdTitleMap.get(series.id());
        String[] titles = titleList.stream().map(FindAliasTitle.Title::title).toArray(String[]::new);

        if (this.equalsTitle(episode.titleInfo().name(), titles)) {
            log.info("别名直接匹配剧集: {}, 通过种子标题解析出的信息为: {}", series.title(), episode);
            return Optional.of(this.parseEntry(entry, series, episode));
        }

        if (this.similarTitle(episode.titleInfo().name(), titles)) {
            log.info("别名模糊匹配剧集: {}, 通过种子标题解析出的信息为: {}", series.title(), episode);
            return Optional.of(this.parseEntry(entry, series, episode));
        }

        return Optional.empty();
    }

    private void cachedSonarrIdTitleMap(List<Series> monitoredSeries) {
        try {
            SonarrIdAliasTitleCached cached;
            if (Files.exists(Path.of(SONARR_ID_ALIAS_TITLE_CACHED_FILE_PATH))) {
                String json = FileUtil.readString(SONARR_ID_ALIAS_TITLE_CACHED_FILE_PATH);
                cached = JSON.parseObject(json, SonarrIdAliasTitleCached.class);
                cached.titles.forEach(title -> sonarrIdTitleMap.put(title.sonarrId(), title.titles));
            } else {
                cached = null;
            }
            // 缓存文件不存在或者缓存已过期
            if (Objects.isNull(cached) || cached.timestamp.plusSeconds(DEFAULT_VALID_SECONDS).isBefore(Instant.now())) {
                List<SonarrIdAliasTitle> titles = new ArrayList<>();
                for (Series series : monitoredSeries) {
                    FindById.TvResult tvResult = null;
                    if (Objects.nonNull(series.imdbId())) {
                        tvResult = theMovieDbApi.findByImdbId(series.imdbId());
                    } else if (Objects.nonNull(series.tvdbId())) {
                        tvResult = theMovieDbApi.findByTvdbId(String.valueOf(series.tvdbId()));
                    }

                    if (Objects.nonNull(tvResult)) {
                        List<FindAliasTitle.Title> titleList = theMovieDbApi.findAliasTitles(tvResult.id());
                        if (!titleList.isEmpty()) {
                            sonarrIdTitleMap.put(series.id(), titleList);
                            titles.add(new SonarrIdAliasTitle(series.id(), titleList));
                            continue;
                        }
                    }
                    sonarrIdTitleMap.put(series.id(), Collections.emptyList());
                    titles.add(new SonarrIdAliasTitle(series.id(), Collections.emptyList()));
                }
                SonarrIdAliasTitleCached newCached = new SonarrIdAliasTitleCached(titles, Instant.now());
                FileUtil.writeString(SONARR_ID_ALIAS_TITLE_CACHED_FILE_PATH, JSON.toJSONString(newCached));
            }
        } catch (IOException e) {
            log.error("读取 Sonarr ID 和别名标题缓存文件失败", e);
            throw new RuntimeException(e);
        }
    }

    private boolean equalsTitle(String title, String... titles) {
        if (Objects.isNull(title) || Objects.isNull(titles)) {
            return false;
        }
        for (String t : titles) {
            if (title.equalsIgnoreCase(t)) {
                return true;
            }
        }
        return false;
    }

    private boolean similarTitle(String title, String... titles) {
        if (Objects.isNull(title) || Objects.isNull(titles)) {
            return false;
        }
        for (String t : titles) {
            if (FuzzUtil.isSimilar(title.toLowerCase(), t.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解析 RSS 条目并转换为 Item 对象
     */
    private Item parseEntry(SyndEntry entry, Series series, RawParser.Episode episode) {
        String mappingUrl = settingsManager.get().getBasic().getMappingUrl();

        int episodeNum = episode.episodeInfo().episode();
        int seasonNum = episode.seasonInfo().season();

        String episodeStr = String.valueOf(episodeNum);

        // 如果不是第一季，则可能需要计算相对集数（区别于绝对集数）
        if (seasonNum > 1) {
            Series seriesDetail = sonarrV3Api.getSeries(series.id());
            if (!CollectionUtils.isEmpty(seriesDetail.seasons())) {
                Integer preEpisodeNumbers = seriesDetail.seasons().stream()
                        // 去掉特典
                        .filter(season -> !season.seasonNumber().equals(0))
                        .sorted(Comparator.comparing(Series.Season::seasonNumber))
                        .limit(episode.seasonInfo().season() - 1)
                        .map(season -> season.statistics().totalEpisodeCount())
                        .reduce(0, Integer::sum);
                if (episode.episodeInfo().episode() > preEpisodeNumbers) {
                    // 计算相对集数
                    int relativeEpisode = episodeNum - preEpisodeNumbers;
                    episodeStr = String.valueOf(relativeEpisode);
                }
            }
        }

        String title = EpisodeTitleUtil.formatEpisodeTitle(series.title(), seasonNum, episodeStr, DEFAULT_QUALITY, DEFAULT_LANGUAGE);

        String link = FeedUtils.getLink(entry);
        String guid = FeedUtils.getUri(entry);

        Date pubDate = resolvePubDateUtil.resolvePubDate(entry, link);

        List<SyndEnclosure> enclosures = FeedUtils.getEnclosures(entry);
        SyndEnclosure first = enclosures.getFirst();
        String url = GetTorrentLinkUtil.formatLink(mappingUrl, first.getUrl(), title, episodeStr, seasonNum, series.id());

        Enclosure enclosure = new Enclosure(url, first.getLength(), first.getType());
        return new Item(title, link, pubDate, guid, enclosure);
    }

    /**
     * 获取 Sonarr 中监控的剧集列表
     */
    private List<Series> getMonitoredSeries() {
        List<Series> seriesList = sonarrV3Api.getSeriesList();
        if (CollectionUtils.isEmpty(seriesList)) {
            return Collections.emptyList();
        }
        return seriesList.stream().filter(Series::monitored).collect(Collectors.toList());
    }

    /**
     * 缓存 Sonarr ID 和别名标题的映射关系
     *
     * @param titles
     * @param timestamp
     */
    private record SonarrIdAliasTitleCached(List<SonarrIdAliasTitle> titles, Instant timestamp) {

    }

    /**
     * Sonarr ID 和别名标题的映射关系
     *
     * @param sonarrId
     * @param titles
     */
    private record SonarrIdAliasTitle(String sonarrId, List<FindAliasTitle.Title> titles) {

    }
}
