package com.github.nekolr.peashooter.service.impl;

import com.github.nekolr.peashooter.api.sonarr.SonarrV3Api;
import com.github.nekolr.peashooter.api.sonarr.req.AddRssIndexer;
import com.github.nekolr.peashooter.api.sonarr.rsp.Series;
import com.github.nekolr.peashooter.api.themoviedb.TheMovieDbApi;
import com.github.nekolr.peashooter.api.themoviedb.rsp.FindById;
import com.github.nekolr.peashooter.config.SettingsManager;
import com.github.nekolr.peashooter.entity.domain.SeriesName;
import com.github.nekolr.peashooter.entity.dto.SeriesNameDto;
import com.github.nekolr.peashooter.service.ISeriesNameService;
import com.github.nekolr.peashooter.service.ISonarrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
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
    @Caching(evict = {@CacheEvict(key = "'all'", beforeInvocation = true)}, cacheable = {@Cacheable(key = "'all'")})
    public List<SeriesNameDto> refreshSeriesName() {
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
    @CacheEvict(key = "'all'", beforeInvocation = true)
    public void syncSeriesLatest() {
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
    public Boolean setupAllGroupIndexer() {
        String apiKey = settingsManager.get().getBasic().getApiKey();
        String mappingUrl = settingsManager.get().getBasic().getMappingUrl();
        String baseUrl = getAllGroupLink(mappingUrl) + "?apiKey=" + apiKey;
        AddRssIndexer indexer = new AddRssIndexer(APPLICATION_NAME, baseUrl);
        indexer.setupDefaultFields();
        return sonarrV3Api.addRssIndexer(indexer);
    }
}
