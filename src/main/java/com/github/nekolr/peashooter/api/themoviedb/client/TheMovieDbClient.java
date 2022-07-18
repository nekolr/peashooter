package com.github.nekolr.peashooter.api.themoviedb.client;

import com.alibaba.fastjson2.JSON;
import com.github.nekolr.peashooter.api.themoviedb.TheMovieDbApi;
import com.github.nekolr.peashooter.api.themoviedb.rsp.FindSeries;
import com.github.nekolr.peashooter.api.themoviedb.rsp.FindSeries.TvResult;
import com.github.nekolr.peashooter.config.SettingsManager;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import jodd.http.ProxyInfo;
import jodd.http.net.SocketHttpConnectionProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;

@Slf4j
@Component
@RequiredArgsConstructor
public class TheMovieDbClient implements TheMovieDbApi {

    private final SettingsManager settingsManager;
    private static final String DEFAULT_LANGUAGE = "zh-CN";
    private static final String DEFAULT_EXTERNAL_SOURCE = "imdb_id";
    private static final String TVDB_EXTERNAL_SOURCE = "tvdb_id";
    private static final String FIND_SERIES_URI_PARAM_API_KEY = "api_key";
    private static final String FIND_SERIES_URI_PARAM_LANGUAGE = "language";
    private static final String FIND_SERIES_URI_PARAM_EXTERNAL_SOURCE = "external_source";

    @Override
    public TvResult findByImdbId(String imdbId) {
        return this.getTvResult(imdbId, DEFAULT_EXTERNAL_SOURCE);
    }

    @Override
    public TvResult findByTvdbId(String tvdbId) {
        return this.getTvResult(tvdbId, TVDB_EXTERNAL_SOURCE);
    }

    private TvResult getTvResult(String id, String externalSource) {
        String apiKey = settingsManager.get().getTheMovieDb().getApiKey();
        Boolean useProxy = settingsManager.get().getTheMovieDb().getUseProxy();
        String uri = MessageFormat.format(FIND_SERIES_URI, id);
        HttpRequest request = HttpRequest.get(this.getUrl(uri));
        request.query(FIND_SERIES_URI_PARAM_API_KEY, apiKey);
        request.query(FIND_SERIES_URI_PARAM_EXTERNAL_SOURCE, externalSource);
        request.query(FIND_SERIES_URI_PARAM_LANGUAGE, DEFAULT_LANGUAGE);
        return this.doSend(request, useProxy);
    }

    private TvResult doSend(HttpRequest request, boolean useProxy) {
        if (useProxy) {
            this.setupProxy(request);
        }
        HttpResponse response = request.send();
        if (response.statusCode() != 200) {
            return null;
        }
        FindSeries findSeries = JSON.parseObject(response.bodyText(), FindSeries.class);
        if (!CollectionUtils.isEmpty(findSeries.tv_results())) {
            return findSeries.tv_results().get(0);
        }
        return null;
    }

    private void setupProxy(HttpRequest request) {
        String proxyIp = settingsManager.get().getHttpProxy().getIp();
        Integer proxyPort = settingsManager.get().getHttpProxy().getPort();
        SocketHttpConnectionProvider provider = new SocketHttpConnectionProvider();
        provider.useProxy(ProxyInfo.httpProxy(proxyIp, proxyPort, null, null));
        request.withConnectionProvider(provider);
    }

    private String getUrl(String uri) {
        return THE_MOVIE_DB_HOST + uri;
    }
}
