package com.github.nekolr.peashooter.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.nekolr.peashooter.api.sonarr.SonarrV3Api;
import com.github.nekolr.peashooter.api.sonarr.request.AddWebhookNotification;
import com.github.nekolr.peashooter.api.sonarr.request.AddRssIndexer;
import com.github.nekolr.peashooter.api.sonarr.request.AddTorznabIndexer;
import com.github.nekolr.peashooter.api.sonarr.request.NotificationField;
import com.github.nekolr.peashooter.api.sonarr.response.Indexer;
import com.github.nekolr.peashooter.api.sonarr.response.Notification;
import com.github.nekolr.peashooter.api.sonarr.response.Series;
import com.github.nekolr.peashooter.api.themoviedb.TheMovieDbApi;
import com.github.nekolr.peashooter.api.themoviedb.response.FindById;
import com.github.nekolr.peashooter.config.SettingsManager;
import com.github.nekolr.peashooter.entity.domain.SeriesName;
import com.github.nekolr.peashooter.dto.SeriesNameDto;
import com.github.nekolr.peashooter.service.ISeriesNameService;
import com.github.nekolr.peashooter.service.ISonarrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.nekolr.peashooter.constant.Peashooter.*;

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "series")
public class SonarrServiceImpl implements ISonarrService {

    private final Map<String, SeriesNameDto> sonarrSeries = new ConcurrentHashMap<>();

    /**
     * tvdb id 映射缓存，短 TTL：Sonarr 新增剧集后应尽快可被 tvdbid 检索命中
     */
    private final Cache<String, Map<String, Long>> tvdbIdMapCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(10))
            .maximumSize(1)
            .build();

    private final SonarrV3Api sonarrV3Api;
    private final TheMovieDbApi theMovieDbApi;
    private final SettingsManager settingsManager;
    private final ISeriesNameService seriesNameService;

    @Override
    @Cacheable(key = "'all'")
    public List<SeriesNameDto> getSeriesNameList() {
        List<Series> seriesList = sonarrV3Api.getSeriesList();
        if (CollectionUtils.isEmpty(seriesList)) {
            return Collections.emptyList();
        } else {
            Stream<SeriesNameDto> stream = seriesList.stream()
                    .filter(series -> sonarrSeries.containsKey(series.id()))
                    .map(series -> sonarrSeries.get(series.id()))
                    .sorted(Comparator.comparing(SeriesNameDto::seriesId, Comparator.comparing(Long::valueOf)).reversed());
            return stream.collect(Collectors.toList());
        }
    }

    @Override
    @Caching(evict = {@CacheEvict(key = "'all'", beforeInvocation = true)},
            cacheable = {@Cacheable(key = "'all'")})
    public List<SeriesNameDto> refreshSeriesNameList() {
        tvdbIdMapCache.invalidateAll();
        List<Series> seriesList = sonarrV3Api.getSeriesList();
        if (CollectionUtils.isEmpty(seriesList)) {
            return Collections.emptyList();
        } else {
            Stream<SeriesNameDto> stream = seriesList.stream().map(series -> {
                String seriesId = series.id();
                if (!sonarrSeries.containsKey(seriesId)) {
                    log.info("原始剧集信息：{}", series);
                    SeriesName seriesName = seriesNameService.findByTitleEn(series.title());
                    if (Objects.isNull(seriesName)) {
                        Optional<FindById.TvResult> tvResultOp = this.findTvById(series);
                        if (tvResultOp.isPresent()) {
                            log.info("获取到对应的中文剧集信息：{}", tvResultOp.get());
                            sonarrSeries.computeIfAbsent(seriesId, k -> {
                                SeriesNameDto seriesNameDto = new SeriesNameDto(seriesId, tvResultOp.get().name(), series.title());
                                seriesNameService.saveSeriesName(new SeriesName(series.title(), tvResultOp.get().name()));
                                return seriesNameDto;
                            });
                        } else {
                            log.info("没有获取到剧集 {} 对应的中文信息", series.title());
                            sonarrSeries.putIfAbsent(seriesId, new SeriesNameDto(seriesId, series.title(), series.title()));
                        }
                    } else {
                        SeriesNameDto dto = new SeriesNameDto(seriesId, seriesName.getTitleZhCN(), seriesName.getTitleEn());
                        sonarrSeries.put(seriesId, dto);
                    }
                }
                return sonarrSeries.get(seriesId);
            }).sorted(Comparator.comparing(SeriesNameDto::seriesId, Comparator.comparing(Long::valueOf)).reversed());

            return stream.collect(Collectors.toList());
        }
    }

    @Override
    @Caching(evict = {@CacheEvict(key = "'all'", beforeInvocation = true)})
    public void syncSeriesNameList() {
        tvdbIdMapCache.invalidateAll();
        final long REFRESH_COUNT = 100;
        List<Series> seriesList = sonarrV3Api.getSeriesList();
        if (CollectionUtils.isEmpty(seriesList)) {
            return;
        }

        Comparator<Series> comparator = Comparator.comparing(Series::id,
                Comparator.comparing(Long::valueOf)).reversed();
        seriesList.stream().sorted(comparator).limit(REFRESH_COUNT).forEach(series -> {
            String seriesId = series.id();
            Optional<FindById.TvResult> tvResultOp = this.findTvById(series);
            if (tvResultOp.isPresent() && StringUtils.hasText(tvResultOp.get().name())) {
                sonarrSeries.put(seriesId, new SeriesNameDto(seriesId, tvResultOp.get().name(), series.title()));
                SeriesName seriesName = seriesNameService.findByTitleEn(series.title());
                if (Objects.nonNull(seriesName)) {
                    if (!tvResultOp.get().name().equals(seriesName.getTitleZhCN())) {
                        seriesName.setTitleZhCN(tvResultOp.get().name());
                        seriesNameService.saveSeriesName(seriesName);
                    }
                } else {
                    seriesNameService.saveSeriesName(new SeriesName(series.title(), tvResultOp.get().name()));
                }
            }
        });
    }

    private Optional<FindById.TvResult> findTvById(Series series) {
        if (Objects.nonNull(series.imdbId())) {
            return theMovieDbApi.findByImdbId(series.imdbId());
        } else if (Objects.nonNull(series.tvdbId())) {
            return theMovieDbApi.findByTvdbId(String.valueOf(series.tvdbId()));
        }
        return Optional.empty();
    }

    @Override
    public Map<String, Long> getSeriesTvdbIdMap() {
        Map<String, Long> map = tvdbIdMapCache.get("all", key -> fetchSeriesTvdbIdMap());
        return Objects.isNull(map) ? Collections.emptyMap() : map;
    }

    private Map<String, Long> fetchSeriesTvdbIdMap() {
        List<Series> seriesList = sonarrV3Api.getSeriesList();
        // 拉取失败时抛异常，Caffeine 的 load 函数抛异常不会写入缓存
        if (Objects.isNull(seriesList)) {
            throw new IllegalStateException("获取 Sonarr 剧集列表失败");
        }
        if (seriesList.isEmpty()) {
            return Collections.emptyMap();
        }
        return seriesList.stream()
                .filter(series -> Objects.nonNull(series.tvdbId()))
                .collect(Collectors.toMap(Series::id, Series::tvdbId, (a, b) -> a));
    }

    @Override
    public Boolean setupAllGroupIndexer() {
        String apiKey = settingsManager.get().getBasic().getApiKey();
        String mappingUrl = settingsManager.get().getBasic().getMappingUrl();
        String baseUrl = getAllGroupRssFileUrl(mappingUrl) + "?" + API_KEY_PARAM + "=" + apiKey;
        AddRssIndexer indexer = new AddRssIndexer(APPLICATION_NAME, baseUrl);
        indexer.setupDefaultFields();
        return sonarrV3Api.addRssIndexer(indexer);
    }

    @Override
    public Boolean setupTorznabIndexer() {
        String name = APPLICATION_NAME + " Torznab";
        String apiKey = settingsManager.get().getBasic().getApiKey();
        String mappingUrl = settingsManager.get().getBasic().getMappingUrl();

        AddTorznabIndexer indexer = new AddTorznabIndexer(name, mappingUrl, NEWZNAB_API_PATH, apiKey);
        indexer.setupDefaultFields();

        // 同名索引器已存在时比较配置：一致跳过，不一致（映射地址或 api key 轮换后）则更新
        Optional<Indexer> existing = sonarrV3Api.getIndexers().stream()
                .filter(i -> name.equals(i.name()))
                .findFirst();
        if (existing.isPresent()) {
            if (isConfigMatched(existing.get(), mappingUrl, apiKey)) {
                log.info("{} 已存在且配置一致，跳过更新", name);
                return Boolean.TRUE;
            }
            log.info("{} 已存在但配置不一致，更新索引器", name);
            indexer.setId(existing.get().id());
            return sonarrV3Api.updateTorznabIndexer(indexer);
        }

        return sonarrV3Api.addTorznabIndexer(indexer);
    }

    /**
     * 检查已有索引器的 baseUrl / apiKey 是否与当前配置一致
     */
    private boolean isConfigMatched(Indexer existing, String mappingUrl, String apiKey) {
        return Objects.equals(getFieldValue(existing, "baseUrl"), mappingUrl)
                && Objects.equals(getFieldValue(existing, "apiKey"), apiKey);
    }

    private String getFieldValue(Indexer indexer, String fieldName) {
        if (Objects.isNull(indexer) || CollectionUtils.isEmpty(indexer.fields())) {
            return null;
        }
        return indexer.fields().stream()
                .filter(field -> fieldName.equals(field.getName()))
                .map(AddRssIndexer.Field::getValue)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void setupOnGrabWebhook() {
        String apiKey = settingsManager.get().getBasic().getApiKey();
        String mappingUrl = settingsManager.get().getBasic().getMappingUrl();

        String webhookUrl = getWebhookUrl(mappingUrl);

        List<Notification> existingNotifications = sonarrV3Api.getNotifications();
        boolean webhookExists = existingNotifications.stream()
                .anyMatch(n -> ON_GRAB_WEBHOOK_NAME.equals(n.name()));

        if (webhookExists) {
            log.info("{} 已存在，跳过创建", ON_GRAB_WEBHOOK_NAME);
            return;
        }

        List<NotificationField> fields = List.of(
                new NotificationField(
                        "url",
                        "Webhook URL",
                        null,
                        webhookUrl + "?" + API_KEY_PARAM + "=" + apiKey,
                        "url",
                        false,
                        null,
                        "normal",
                        false
                ),
                new NotificationField(
                        "method",
                        "Method",
                        "Which HTTP method to use submit to Webservice",
                        1,
                        "select",
                        false,
                        List.of(new NotificationField.SelectOption(1, "POST"),
                                new NotificationField.SelectOption(2, "PUT")),
                        "normal",
                        false
                ));

        AddWebhookNotification notification = new AddWebhookNotification();
        notification.setName(ON_GRAB_WEBHOOK_NAME);
        notification.setFields(fields);
        notification.setOnGrab(true);
        notification.setImplementationName("Webhook");
        notification.setImplementation("Webhook");
        notification.setConfigContract("WebhookSettings");

        sonarrV3Api.addWebhookNotification(notification);
    }

    private String getWebhookUrl(String mappingUrl) {
        if (mappingUrl.endsWith("/")) {
            mappingUrl = mappingUrl.substring(0, mappingUrl.length() - 1);
        }
        return mappingUrl + "/api/torrents/onGrab";
    }
}
