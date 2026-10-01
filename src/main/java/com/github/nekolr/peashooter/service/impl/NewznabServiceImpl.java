package com.github.nekolr.peashooter.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.nekolr.peashooter.config.Settings;
import com.github.nekolr.peashooter.config.SettingsManager;
import com.github.nekolr.peashooter.entity.domain.Group;
import com.github.nekolr.peashooter.newznab.NewznabXmlWriter;
import com.github.nekolr.peashooter.newznab.model.NewznabItem;
import com.github.nekolr.peashooter.newznab.model.NewznabSearchCriteria;
import com.github.nekolr.peashooter.repository.GroupRepository;
import com.github.nekolr.peashooter.rss.loader.RssLoader;
import com.github.nekolr.peashooter.service.INewznabService;
import com.github.nekolr.peashooter.service.ISonarrService;
import com.github.nekolr.peashooter.util.FeedUtils;
import com.github.nekolr.peashooter.util.MagnetUtils;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.github.nekolr.peashooter.constant.Peashooter.*;

/**
 * Newznab/Torznab 索引器服务实现
 * <p>
 * 数据来源为已转换的分组 RSS 文件（peashooter/rss/group/{id}.xml）以及自动分组文件（automated.xml）。
 * 这些文件中不含剧集/季度/集数信息，季度与集数从标题反解，剧集 id 取自所属分组。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewznabServiceImpl implements INewznabService {

    /**
     * 从标题中解析季度与集数，形如：Show - S1E01 - Episode 01 - Chinese - WEBDL-1080p
     */
    private static final Pattern SEASON_EPISODE_PATTERN = Pattern.compile("(?i)S(\\d{1,3})E(\\d{1,4})\\b");

    /**
     * 条目缓存 key（只有全量一份）
     */
    private static final String ITEMS_CACHE_KEY = "all";

    private final RssLoader rssLoader;
    private final GroupRepository groupRepository;
    private final SettingsManager settingsManager;
    private final ISonarrService sonarrService;
    private final NewznabXmlWriter xmlWriter;

    /**
     * 聚合结果缓存，短 TTL，避免每次搜索都全量读盘解析
     */
    private final Cache<String, List<NewznabItem>> itemsCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(NEWZNAB_ITEMS_CACHE_SECONDS))
            .maximumSize(1)
            .build();

    @Override
    public String getCaps() {
        return xmlWriter.writeCaps(getNewznabUrl(mappingUrl()));
    }

    @Override
    public String search(NewznabSearchCriteria criteria) {
        String baseUrl = getNewznabUrl(mappingUrl());

        boolean hasImdb = StringUtils.hasText(criteria.imdbid());
        boolean hasTvdb = StringUtils.hasText(criteria.tvdbid());
        boolean hasQ = StringUtils.hasText(criteria.q());
        // 仅提供了 imdb id，无法映射，返回空结果
        if (hasImdb && !hasTvdb && !hasQ) {
            return xmlWriter.writeSearchResult(Collections.emptyList(), 0, 0, baseUrl);
        }

        // tvdb id 不写入条目缓存，在此处实时解析；解析失败仅影响当次请求
        Map<String, Long> tvdbIdMap = tryGetTvdbIdMap();
        if (hasTvdb && Objects.isNull(tvdbIdMap)) {
            // Sonarr 暂不可用，无法按 tvdbid 过滤，返回空结果
            log.warn("tvdb id 映射不可用，tvdbid 检索返回空结果");
            return xmlWriter.writeSearchResult(Collections.emptyList(), 0, 0, baseUrl);
        }

        Stream<NewznabItem> stream = collectItems().stream();

        if (hasQ) {
            // Newznab 的 q 为空格分隔关键字，AND 语义：每个关键字都必须命中标题
            String[] keywords = criteria.q().trim().toLowerCase(Locale.ROOT).split("\\s+");
            stream = stream.filter(item -> item.title() != null && containsAll(item.title(), keywords));
        }
        if (hasTvdb) {
            stream = stream.filter(item -> item.seriesId() != null
                    && criteria.tvdbid().equals(String.valueOf(tvdbIdMap.get(item.seriesId()))));
        }
        if (Objects.nonNull(criteria.season())) {
            stream = stream.filter(item -> criteria.season().equals(item.season()));
        }
        if (Objects.nonNull(criteria.ep())) {
            stream = stream.filter(item -> criteria.ep().equals(item.episode()));
        }

        List<NewznabItem> matched = stream
                .sorted(Comparator.comparing(NewznabItem::pubDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        int offset = Math.max(0, criteria.offset());
        int limit = criteria.limit() <= 0 ? NEWZNAB_DEFAULT_LIMIT
                : Math.min(criteria.limit(), NEWZNAB_MAX_LIMIT);
        List<NewznabItem> page = matched.stream().skip(offset).limit(limit).toList();

        // 输出前补齐 tvdb id 属性
        if (Objects.nonNull(tvdbIdMap)) {
            page = page.stream()
                    .map(item -> item.withTvdbId(item.seriesId() == null ? null : tvdbIdMap.get(item.seriesId())))
                    .toList();
        }

        return xmlWriter.writeSearchResult(page, offset, matched.size(), baseUrl);
    }

    @Override
    public String getError(int code, String description) {
        return xmlWriter.writeError(code, description);
    }

    /**
     * 聚合所有分组与自动分组的条目（带短 TTL 缓存）
     */
    private List<NewznabItem> collectItems() {
        List<NewznabItem> cached = itemsCache.get(ITEMS_CACHE_KEY, key -> loadItems());
        return Objects.isNull(cached) ? Collections.emptyList() : cached;
    }

    /**
     * 按 guid 去重（分组条目优先）聚合所有分组与自动分组的条目
     */
    private List<NewznabItem> loadItems() {
        Map<String, NewznabItem> byGuid = new LinkedHashMap<>();

        for (Group group : groupRepository.findAll()) {
            for (SyndEntry entry : loadEntries(getGroupRssFilepath(group.getId()))) {
                NewznabItem item = toItem(entry, group.getReferenceId());
                if (Objects.nonNull(item)) {
                    byGuid.putIfAbsent(item.guid(), item);
                }
            }
        }

        for (SyndEntry entry : loadEntries(getAutomatedGroupRssFilepath())) {
            NewznabItem item = toItem(entry, null);
            if (Objects.nonNull(item)) {
                byGuid.putIfAbsent(item.guid(), item);
            }
        }

        return new ArrayList<>(byGuid.values());
    }

    private NewznabItem toItem(SyndEntry entry, String seriesId) {
        String title = FeedUtils.getTitle(entry);
        String guid = FeedUtils.getUri(entry);
        if (!StringUtils.hasText(title) || !StringUtils.hasText(guid)) {
            return null;
        }

        List<SyndEnclosure> enclosures = FeedUtils.getEnclosures(entry);
        String downloadUrl;
        String enclosureType;
        Long length;
        if (!enclosures.isEmpty()) {
            SyndEnclosure first = enclosures.getFirst();
            downloadUrl = FeedUtils.getEnclosureUrl(first);
            enclosureType = FeedUtils.getEnclosureType(first);
            length = FeedUtils.getEnclosureLength(first);
        } else {
            downloadUrl = FeedUtils.getLink(entry);
            enclosureType = null;
            length = null;
        }
        if (!StringUtils.hasText(downloadUrl)) {
            return null;
        }
        // 磁力链接的体积通常为 1（未知），统一记为 0
        long size = (Objects.isNull(length) || length <= 1L) ? 0L : length;

        Date pubDate = FeedUtils.getPublishedDate(entry);

        Integer season = null;
        Integer episode = null;
        Matcher matcher = SEASON_EPISODE_PATTERN.matcher(title);
        if (matcher.find()) {
            season = Integer.parseInt(matcher.group(1));
            episode = Integer.parseInt(matcher.group(2));
        }

        Long tvdbId = null;
        boolean magnet = MagnetUtils.isMagnet(downloadUrl);

        return new NewznabItem(title, guid, pubDate, downloadUrl, enclosureType, size,
                NEWZNAB_ITEM_CATEGORIES, seriesId, tvdbId, season, episode,
                magnet ? downloadUrl : null, magnet ? MagnetUtils.extractInfoHash(downloadUrl) : null);
    }

    /**
     * 读取并解析分组 RSS 文件，文件不存在或解析失败时返回空列表
     */
    private List<SyndEntry> loadEntries(String filepath) {
        if (!Files.exists(Paths.get(filepath))) {
            return Collections.emptyList();
        }
        try {
            String xml = rssLoader.loadFromFile(filepath);
            if (!StringUtils.hasText(xml)) {
                return Collections.emptyList();
            }
            SyndFeed feed = FeedUtils.getFeed(xml);
            return FeedUtils.getEntries(feed);
        } catch (Exception e) {
            log.warn("解析分组 RSS 文件失败：{}", filepath, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取剧集 tvdb id 映射，失败时返回 null（异常不会写入任何缓存）
     */
    private Map<String, Long> tryGetTvdbIdMap() {
        try {
            return sonarrService.getSeriesTvdbIdMap();
        } catch (Exception e) {
            log.warn("获取剧集 tvdb id 映射失败，tvdbid 检索暂不可用", e);
            return null;
        }
    }

    /**
     * 大小写不敏感地检查标题是否包含全部关键字（AND 语义）
     */
    private boolean containsAll(String title, String[] keywords) {
        String lower = title.toLowerCase(Locale.ROOT);
        for (String keyword : keywords) {
            if (keyword.isEmpty() || !lower.contains(keyword)) {
                return false;
            }
        }
        return true;
    }

    private String mappingUrl() {
        Settings.Basic basic = settingsManager.get().getBasic();
        if (Objects.isNull(basic) || Objects.isNull(basic.getMappingUrl())) {
            return "";
        }
        return basic.getMappingUrl();
    }
}
