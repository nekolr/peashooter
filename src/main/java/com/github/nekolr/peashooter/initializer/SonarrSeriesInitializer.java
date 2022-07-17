package com.github.nekolr.peashooter.initializer;

import com.github.nekolr.peashooter.api.sonarr.SonarrApi;
import com.github.nekolr.peashooter.api.sonarr.rsp.Series;
import com.github.nekolr.peashooter.api.themoviedb.TheMovieDbApi;
import com.github.nekolr.peashooter.api.themoviedb.rsp.FindSeries.TvResult;
import com.github.nekolr.peashooter.entity.SeriesZhCN;
import com.github.nekolr.peashooter.service.ISonarrService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
@DependsOn("settingsInitializer")
@RequiredArgsConstructor
public class SonarrSeriesInitializer implements InitializingBean {

    private final SonarrApi sonarrApi;
    private final TheMovieDbApi theMovieDbApi;
    private final ISonarrService sonarrService;

    @Override
    public void afterPropertiesSet() {
        initSonarrSeries();
    }

    private void initSonarrSeries() {
        List<Series> seriesList = sonarrApi.getSeriesList();
        if (!CollectionUtils.isEmpty(seriesList)) {
            seriesList.forEach(series -> {
                TvResult tvResult = theMovieDbApi.findByImdbId(series.imdbId());
                sonarrService.setSeriesZhCN(series.id(), new SeriesZhCN(series.id(), tvResult.name(), series.title()));
            });
        }
    }
}
