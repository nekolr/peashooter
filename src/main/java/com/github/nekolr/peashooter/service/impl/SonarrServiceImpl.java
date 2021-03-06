package com.github.nekolr.peashooter.service.impl;

import com.github.nekolr.peashooter.api.sonarr.SonarrApi;
import com.github.nekolr.peashooter.api.sonarr.SonarrV3Api;
import com.github.nekolr.peashooter.api.sonarr.req.AddRssIndexer;
import com.github.nekolr.peashooter.api.sonarr.rsp.Series;
import com.github.nekolr.peashooter.api.themoviedb.TheMovieDbApi;
import com.github.nekolr.peashooter.api.themoviedb.rsp.FindSeries.TvResult;
import com.github.nekolr.peashooter.config.SettingsManager;
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

import static com.github.nekolr.peashooter.constant.Peashooter.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SonarrServiceImpl implements ISonarrService {

    private Map<String, SeriesZhCN> sonarrSeries = new ConcurrentHashMap();

    private final SonarrApi sonarrApi;
    private final SonarrV3Api sonarrV3Api;
    private final TheMovieDbApi theMovieDbApi;
    private final SettingsManager settingsManager;

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
        log.info("???????????? sonarr ?????????????????????");
        List<Series> seriesList = sonarrApi.getSeriesList();
        if (CollectionUtils.isEmpty(seriesList)) {
            log.info("??????????????? sonarr ???????????????");
            return Collections.emptyList();
        } else {
            Stream<SeriesZhCN> stream = seriesList.stream().map(series -> {
                String seriesId = series.id();
                if (!this.hasSeriesZhCN(seriesId)) {
                    log.info("?????????????????????{}", series);
                    TvResult tvResult = null;
                    if (Objects.nonNull(series.imdbId())) {
                        tvResult = theMovieDbApi.findByImdbId(series.imdbId());
                    } else if (Objects.nonNull(series.tvdbId())) {
                        tvResult = theMovieDbApi.findByTvdbId(String.valueOf(series.tvdbId()));
                    }
                    if (Objects.nonNull(tvResult)) {
                        log.info("???????????????????????????????????????{}", tvResult);
                        this.setSeriesZhCN(seriesId, new SeriesZhCN(seriesId, tvResult.name(), series.title()));
                    } else {
                        log.warn("????????????????????? {} ?????????????????????", series.title());
                        this.setSeriesZhCN(seriesId, new SeriesZhCN(seriesId, series.title(), series.title()));
                    }
                }
                return sonarrSeries.get(seriesId);
            }).sorted(Comparator.comparing(SeriesZhCN::seriesId).reversed());

            List<SeriesZhCN> result = stream.collect(Collectors.toList());
            log.info("????????????????????????");
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
