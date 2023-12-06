package com.github.nekolr.peashooter.service.impl;

import com.github.nekolr.peashooter.api.sonarr.SonarrApi;
import com.github.nekolr.peashooter.api.sonarr.SonarrV3Api;
import com.github.nekolr.peashooter.api.sonarr.req.AddRssIndexer;
import com.github.nekolr.peashooter.api.sonarr.rsp.Series;
import com.github.nekolr.peashooter.api.themoviedb.TheMovieDbApi;
import com.github.nekolr.peashooter.api.themoviedb.rsp.FindSeries.TvResult;
import com.github.nekolr.peashooter.config.SettingsManager;
import com.github.nekolr.peashooter.entity.domain.SeriesName;
import com.github.nekolr.peashooter.entity.dto.SeriesNameDto;
import com.github.nekolr.peashooter.service.ISeriesNameService;
import com.github.nekolr.peashooter.service.ISonarrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.nekolr.peashooter.constant.Peashooter.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SonarrServiceImpl implements ISonarrService {

    private Map<String, SeriesNameDto> sonarrSeries = new ConcurrentHashMap();

    private final SonarrApi sonarrApi;
    private final SonarrV3Api sonarrV3Api;
    private final TheMovieDbApi theMovieDbApi;
    private final SettingsManager settingsManager;
    private final ISeriesNameService seriesNameService;

    @Override
    public List<SeriesNameDto> getSeriesNameList() {
        List<Series> seriesList = sonarrApi.getSeriesList();
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
    public List<SeriesNameDto> refreshSeriesName() {
        log.info("开始刷新 sonarr 的剧集中文信息");
        List<Series> seriesList = sonarrApi.getSeriesList();
        if (CollectionUtils.isEmpty(seriesList)) {
            log.info("没有获取到 sonarr 的剧集信息");
            return Collections.emptyList();
        } else {
            Stream<SeriesNameDto> stream = seriesList.stream().map(series -> {
                String seriesId = series.id();
                if (!sonarrSeries.containsKey(seriesId)) {
                    log.info("原始剧集信息：{}", series);
                    SeriesName seriesName = seriesNameService.findByTitleJp(series.title());
                    if (Objects.isNull(seriesName)) {
                        TvResult tvResult = null;
                        if (Objects.nonNull(series.imdbId())) {
                            tvResult = theMovieDbApi.findByImdbId(series.imdbId());
                        } else if (Objects.nonNull(series.tvdbId())) {
                            tvResult = theMovieDbApi.findByTvdbId(String.valueOf(series.tvdbId()));
                        }
                        if (Objects.nonNull(tvResult)) {
                            log.info("获取到对应的中文剧集信息：{}", tvResult);
                            sonarrSeries.put(seriesId, new SeriesNameDto(seriesId, tvResult.name(), series.title()));
                            seriesNameService.saveSeriesName(new SeriesName(series.title(), tvResult.name()));
                        } else {
                            log.warn("没有获取到剧集 {} 对应的中文信息", series.title());
                            sonarrSeries.put(seriesId, new SeriesNameDto(seriesId, series.title(), series.title()));
                        }
                    } else {
                        log.info("加载本地剧集信息：{}", seriesName);
                        SeriesNameDto dto = new SeriesNameDto(seriesId, seriesName.getTitleZhCN(), seriesName.getTitleJp());
                        sonarrSeries.put(seriesId, dto);
                    }
                }
                return sonarrSeries.get(seriesId);
            }).sorted(Comparator.comparing(SeriesNameDto::seriesId, Comparator.comparing(Long::valueOf)).reversed());

            List<SeriesNameDto> result = stream.collect(Collectors.toList());
            log.info("剧集信息刷新完毕");
            return result;
        }
    }

    @Override
    public Boolean setupAllGroupIndexer() {
        String apiKey = settingsManager.get().getBasic().getApiKey();
        String mappingUrl = settingsManager.get().getBasic().getMappingUrl();
        String baseUrl = getAllGroupLink(mappingUrl) + QUESTION + API_KEY + EQUALS + apiKey;
        AddRssIndexer indexer = new AddRssIndexer(APPLICATION_NAME, baseUrl);
        indexer.setupDefaultFields();
        return sonarrV3Api.addRssIndexer(indexer);
    }
}
