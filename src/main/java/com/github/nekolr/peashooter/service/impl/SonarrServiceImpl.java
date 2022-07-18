package com.github.nekolr.peashooter.service.impl;

import com.github.nekolr.peashooter.api.sonarr.SonarrApi;
import com.github.nekolr.peashooter.api.sonarr.rsp.Series;
import com.github.nekolr.peashooter.api.themoviedb.TheMovieDbApi;
import com.github.nekolr.peashooter.api.themoviedb.rsp.FindSeries.TvResult;
import com.github.nekolr.peashooter.entity.SeriesZhCN;
import com.github.nekolr.peashooter.service.ISonarrService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SonarrServiceImpl implements ISonarrService {

    private Map<Long, SeriesZhCN> sonarrSeries = new ConcurrentHashMap();

    private final SonarrApi sonarrApi;
    private final TheMovieDbApi theMovieDbApi;

    @Override
    public void setSeriesZhCN(Long id, SeriesZhCN series) {
        sonarrSeries.put(id, series);
    }

    @Override
    public boolean hasSeriesZhCN(Long id) {
        return sonarrSeries.containsKey(id);
    }

    @Override
    public List<SeriesZhCN> getSeriesZhCNList() {
        List<Series> seriesList = sonarrApi.getSeriesList();
        if (CollectionUtils.isEmpty(seriesList)) {
            return Collections.emptyList();
        } else {
            Stream<SeriesZhCN> stream = seriesList.stream().map(series -> {
                Long seriesId = series.id();
                if (!this.hasSeriesZhCN(seriesId)) {
                    TvResult tvResult = theMovieDbApi.findByImdbId(series.imdbId());
                    if (Objects.nonNull(tvResult)) {
                        this.setSeriesZhCN(seriesId, new SeriesZhCN(seriesId, tvResult.name(), series.title()));
                    }
                }
                return sonarrSeries.get(seriesId);
            }).sorted(Comparator.comparing(SeriesZhCN::seriesId).reversed());

            return stream.collect(Collectors.toList());
        }
    }
}
