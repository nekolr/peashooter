package com.github.nekolr.peashooter.service.impl;

import com.github.nekolr.peashooter.api.sonarr.SonarrV3Api;
import com.github.nekolr.peashooter.api.sonarr.response.Series;
import com.github.nekolr.peashooter.api.themoviedb.TheMovieDbApi;
import com.github.nekolr.peashooter.api.themoviedb.response.FindAliasTitle;
import com.github.nekolr.peashooter.api.themoviedb.response.FindById;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private static final List<String> EXCLUDE_SOURCES = List.of("CR", "BILIBILI"); // 排除的源
    private static final Map<String, Integer> SOURCE_WEIGHT_MAP = Map.of("BAHA", 0, "WEB", 1, "B-GLOBAL", 2); // 源优先级权重，越小越高
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

            // 根据源优先级，排除掉不需要的源
            items = this.filterItemsBySourcePriority(items);

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

    /**
     * 根据源优先级，排除掉不需要的源
     *
     * @param items 待过滤的剧集条目
     * @return 过滤后的剧集条目
     */
    private List<Item> filterItemsBySourcePriority(List<Item> items) {
        List<Item> filteredItems = new ArrayList<>();

        Map<String, List<Item>> groupedItems = items.stream()
                .collect(Collectors.groupingBy(item -> item.seriesId() + item.seasonNum() + item.episodeNum()));

        List<String> sources = SOURCE_WEIGHT_MAP.keySet().stream().toList();
        for (Map.Entry<String, List<Item>> entry : groupedItems.entrySet()) {
            List<Item> seriesItems = entry.getValue();

            // 按源优先级过滤
            Optional<Item> highestPriorityItem = seriesItems.stream()
                    .filter(item -> Objects.nonNull(item.source()))
                    .min(Comparator.comparingInt(item -> {
                        Optional<String> optionalSource = sources.stream()
                                .filter(s -> item.source().toUpperCase().contains(s)).findAny();
                        if (optionalSource.isPresent()) {
                            return SOURCE_WEIGHT_MAP.get(optionalSource.get());
                        } else {
                            // 如果源不在优先级中，返回最大值，表示最低优先级
                            return Integer.MAX_VALUE;
                        }
                    }));

            if (highestPriorityItem.isPresent()) {
                filteredItems.add(highestPriorityItem.get());
            } else {
                // 来到这里，说明来源为空，选择第一个发布时间最晚的
                seriesItems.stream()
                        .filter(item -> Objects.isNull(item.source()))
                        .max(Comparator.comparing(Item::pubDate))
                        .ifPresent(filteredItems::add);
            }
        }

        return filteredItems;
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

    private Optional<SonarrIdAliasTitle> getSonarrIdAliasTitle(Series series) {
        Optional<FindById.TvResult> tvResultOp = Optional.empty();
        if (Objects.nonNull(series.imdbId())) {
            tvResultOp = theMovieDbApi.findByImdbId(series.imdbId());
        } else if (Objects.nonNull(series.tvdbId())) {
            tvResultOp = theMovieDbApi.findByTvdbId(String.valueOf(series.tvdbId()));
        }

        if (tvResultOp.isPresent()) {
            List<FindAliasTitle.Title> titleList = theMovieDbApi.findAliasTitles(tvResultOp.get().id());
            if (!titleList.isEmpty()) {
                return Optional.of(new SonarrIdAliasTitle(series.id(), titleList));
            }
        }

        return Optional.of(new SonarrIdAliasTitle(series.id(), Collections.emptyList()));
    }

    private synchronized void cachedSonarrIdTitleMap(List<Series> monitoredSeries) {
        try {
            SonarrIdAliasTitleCached cached = null;
            List<SonarrIdAliasTitle> titles = new ArrayList<>();
            Path cachePath = Paths.get(SONARR_ID_ALIAS_TITLE_CACHED_FILE_PATH);

            if (Files.exists(cachePath)) {
                String json = Files.readString(cachePath, StandardCharsets.UTF_8);
                cached = JacksonUtils.tryParse(() ->
                        JacksonUtils.getObjectMapper().readValue(json, SonarrIdAliasTitleCached.class));

                Map<String, List<FindAliasTitle.Title>> cachedMap = cached.titles().stream()
                        .filter(t -> Objects.nonNull(t.titles))
                        .collect(Collectors.toMap(SonarrIdAliasTitle::sonarrId, SonarrIdAliasTitle::titles, (v1, v2) -> v1));

                // 是否需要更新缓存文件
                boolean needWrite = false;

                for (Series series : monitoredSeries) {
                    boolean exists = cachedMap.containsKey(series.id());
                    if (exists) {
                        sonarrIdTitleMap.put(series.id(), cachedMap.get(series.id()));
                        titles.add(new SonarrIdAliasTitle(series.id(), cachedMap.get(series.id())));
                    } else {
                        Optional<SonarrIdAliasTitle> sonarrIdAliasTitle = this.getSonarrIdAliasTitle(series);
                        if (sonarrIdAliasTitle.isPresent()) {
                            List<FindAliasTitle.Title> titleList = sonarrIdAliasTitle.get().titles();
                            sonarrIdTitleMap.put(series.id(), titleList);
                            titles.add(new SonarrIdAliasTitle(series.id(), titleList));
                        }
                        needWrite = true;
                    }
                }

                if (needWrite) {
                    SonarrIdAliasTitleCached newCached = new SonarrIdAliasTitleCached(titles, cached.timestamp);
                    this.writeCacheFile(newCached);
                }

            }

            // 缓存文件不存在或者缓存已过期
            if (Objects.isNull(cached) || cached.timestamp.plusSeconds(DEFAULT_VALID_SECONDS).isBefore(Instant.now())) {
                for (Series series : monitoredSeries) {
                    Optional<SonarrIdAliasTitle> sonarrIdAliasTitle = this.getSonarrIdAliasTitle(series);
                    if (sonarrIdAliasTitle.isPresent()) {
                        List<FindAliasTitle.Title> titleList = sonarrIdAliasTitle.get().titles();
                        sonarrIdTitleMap.put(series.id(), titleList);
                        titles.add(sonarrIdAliasTitle.get());
                    } else {
                        sonarrIdTitleMap.put(series.id(), Collections.emptyList());
                        titles.add(new SonarrIdAliasTitle(series.id(), Collections.emptyList()));
                    }
                }
                SonarrIdAliasTitleCached newCached = new SonarrIdAliasTitleCached(titles, Instant.now());
                this.writeCacheFile(newCached);
            }
        } catch (IOException e) {
            log.error("读取 Sonarr ID 和别名标题缓存文件失败", e);
            throw new RuntimeException(e);
        }
    }

    private void writeCacheFile(SonarrIdAliasTitleCached cached) {
        try {
            String json = JacksonUtils.tryParse(() ->
                    JacksonUtils.getObjectMapper().writeValueAsString(cached));
            Path cachePath = Paths.get(SONARR_ID_ALIAS_TITLE_CACHED_FILE_PATH);
            // 确保父目录存在
            if (cachePath.getParent() != null) {
                Files.createDirectories(cachePath.getParent());
            }
            Files.writeString(cachePath, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("写入缓存文件失败", e);
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
                int preEpisodeNumbers = seriesDetail.seasons().stream()
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
                    log.info("计算相对集数: {}, 原集数: {}, 计算后集数: {}", series.title(), episodeNum, relativeEpisode);
                }
            }
        }

        String title = EpisodeTitleUtil.formatEpisodeTitle(series.title(), seasonNum, episodeStr, DEFAULT_LANGUAGE, DEFAULT_QUALITY);

        String link = FeedUtils.getLink(entry);
        String guid = FeedUtils.getUri(entry);

        Date pubDate = resolvePubDateUtil.resolvePubDate(entry, link);

        List<SyndEnclosure> enclosures = FeedUtils.getEnclosures(entry);
        SyndEnclosure first = enclosures.getFirst();
        String url = GetTorrentLinkUtil.formatLink(mappingUrl, first.getUrl(), title, episodeStr, seasonNum, series.id());

        Enclosure enclosure = new Enclosure(url, first.getLength(), first.getType());
        return new Item(title, link, pubDate, guid, enclosure, series.id(), seasonNum, Integer.parseInt(episodeStr), episode.source());
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
