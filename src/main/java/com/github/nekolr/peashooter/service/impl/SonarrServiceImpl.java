package com.github.nekolr.peashooter.service.impl;

import com.github.nekolr.peashooter.api.sonarr.SonarrApi;
import com.github.nekolr.peashooter.api.sonarr.rsp.Series;
import com.github.nekolr.peashooter.api.themoviedb.TheMovieDbApi;
import com.github.nekolr.peashooter.api.themoviedb.rsp.FindSeries.TvResult;
import com.github.nekolr.peashooter.entity.SeriesZhCN;
import com.github.nekolr.peashooter.service.ISonarrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class SonarrServiceImpl implements ISonarrService {

    private Map<String, SeriesZhCN> sonarrSeries = new ConcurrentHashMap();

    private final SonarrApi sonarrApi;
    private final TheMovieDbApi theMovieDbApi;

    @Override
    public void setSeriesZhCN(String id, SeriesZhCN series) {
        sonarrSeries.put(id, series);
    }

    @Override
    public boolean hasSeriesZhCN(String id) {
        return sonarrSeries.containsKey(id);
    }

    @Override
    public List<SeriesZhCN> getSeriesZhCNList() {
        List<Series> seriesList = sonarrApi.getSeriesList();
        if (CollectionUtils.isEmpty(seriesList)) {
            return Collections.emptyList();
        } else {
            Stream<SeriesZhCN> stream = seriesList.stream()
                    .filter(series -> this.hasSeriesZhCN(series.id()))
                    .map(series -> sonarrSeries.get(series.id()))
                    .sorted(Comparator.comparing(SeriesZhCN::seriesId).reversed());
            return stream.collect(Collectors.toList());
        }
    }

    @Override
    public List<SeriesZhCN> refreshSeriesZhCNList() {
        log.info("开始刷新 sonarr 的剧集中文信息");
        List<Series> seriesList = sonarrApi.getSeriesList();
        if (CollectionUtils.isEmpty(seriesList)) {
            log.info("没有获取到 sonarr 的剧集信息");
            return Collections.emptyList();
        } else {
            Stream<SeriesZhCN> stream = seriesList.stream().map(series -> {
                String seriesId = series.id();
                if (!this.hasSeriesZhCN(seriesId)) {
                    log.info("原始剧集信息：{}", series);
                    TvResult tvResult = null;
                    if (Objects.nonNull(series.imdbId())) {
                        tvResult = theMovieDbApi.findByImdbId(series.imdbId());
                    } else if (Objects.nonNull(series.tvdbId())) {
                        tvResult = theMovieDbApi.findByTvdbId(String.valueOf(series.tvdbId()));
                    }
                    if (Objects.nonNull(tvResult)) {
                        log.info("获取到对应的中文剧集信息：{}", tvResult);
                        this.setSeriesZhCN(seriesId, new SeriesZhCN(seriesId, tvResult.name(), series.title()));
                    } else {
                        log.warn("没有获取到剧集 {} 对应的中文信息", series.title());
                        this.setSeriesZhCN(seriesId, new SeriesZhCN(seriesId, series.title(), series.title()));
                    }
                }
                return sonarrSeries.get(seriesId);
            }).sorted(Comparator.comparing(SeriesZhCN::seriesId).reversed());

            List<SeriesZhCN> result = stream.collect(Collectors.toList());
            log.info("刷新完毕");
            return result;
        }
    }
}
