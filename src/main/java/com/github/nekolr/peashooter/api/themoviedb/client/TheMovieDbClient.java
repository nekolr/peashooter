package com.github.nekolr.peashooter.api.themoviedb.client;

import com.alibaba.fastjson2.JSON;
import com.github.nekolr.peashooter.api.themoviedb.TheMovieDbApi;
import com.github.nekolr.peashooter.api.themoviedb.rsp.FindAliasTitle;
import com.github.nekolr.peashooter.api.themoviedb.rsp.FindById;
import com.github.nekolr.peashooter.api.themoviedb.rsp.FindByKeyword;
import com.github.nekolr.peashooter.config.SettingsManager;
import jodd.http.HttpException;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import jodd.http.ProxyInfo;
import jodd.http.net.SocketHttpConnectionProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    @Retryable(retryFor = HttpException.class, backoff = @Backoff(multiplier = 1.5))
    public FindById.TvResult findByImdbId(String imdbId) {
        return this.findById(imdbId, DEFAULT_EXTERNAL_SOURCE);
    }

    @Override
    @Retryable(retryFor = HttpException.class, backoff = @Backoff(multiplier = 1.5))
    public FindById.TvResult findByTvdbId(String tvdbId) {
        return this.findById(tvdbId, TVDB_EXTERNAL_SOURCE);
    }

    @Override
    public FindByKeyword.TvResult findByKeyword(String keyword) {
        String apiKey = settingsManager.get().getTheMovieDb().getApiKey();
        Boolean useProxy = settingsManager.get().getTheMovieDb().getUseProxy();
        String uri = MessageFormat.format(FIND_BY_KEYWORD_URI, keyword);
        HttpRequest request = HttpRequest.get(this.getUrl(uri));
        request.query(FIND_SERIES_URI_PARAM_API_KEY, apiKey);
        return this.doFindByKeyword(request, useProxy);
    }

    @Override
    public List<FindAliasTitle.Title> findAliasTitles(Integer seriesId) {
        String apiKey = settingsManager.get().getTheMovieDb().getApiKey();
        Boolean useProxy = settingsManager.get().getTheMovieDb().getUseProxy();
        String uri = MessageFormat.format(FIND_ALIAS_TITLE_URI, String.valueOf(seriesId));
        HttpRequest request = HttpRequest.get(this.getUrl(uri));
        request.query(FIND_SERIES_URI_PARAM_API_KEY, apiKey);
        if (useProxy) {
            this.setupProxy(request);
        }
        HttpResponse response = request.send();
        if (response.statusCode() != 200) {
            return Collections.emptyList();
        }
        FindAliasTitle findAliasTitle = JSON.parseObject(response.bodyText(), FindAliasTitle.class);
        if (!CollectionUtils.isEmpty(findAliasTitle.results())) {
            return findAliasTitle.results();
        }
        return Collections.emptyList();
    }

    private FindById.TvResult findById(String id, String externalSource) {
        String apiKey = settingsManager.get().getTheMovieDb().getApiKey();
        Boolean useProxy = settingsManager.get().getTheMovieDb().getUseProxy();
        String uri = MessageFormat.format(FIND_BY_ID_URI, id);
        HttpRequest request = HttpRequest.get(this.getUrl(uri));
        request.query(FIND_SERIES_URI_PARAM_API_KEY, apiKey);
        request.query(FIND_SERIES_URI_PARAM_LANGUAGE, DEFAULT_LANGUAGE);
        request.query(FIND_SERIES_URI_PARAM_EXTERNAL_SOURCE, externalSource);
        return this.doFindById(request, useProxy);
    }

    private FindById.TvResult doFindById(HttpRequest request, boolean useProxy) {
        if (useProxy) {
            this.setupProxy(request);
        }
        HttpResponse response = request.send();
        if (response.statusCode() != 200) {
            return null;
        }
        FindById findById = JSON.parseObject(response.bodyText(), FindById.class);
        if (!CollectionUtils.isEmpty(findById.tv_results())) {
            return findById.tv_results().getFirst();
        }
        return null;
    }

    private FindByKeyword.TvResult doFindByKeyword(HttpRequest request, boolean useProxy) {
        if (useProxy) {
            this.setupProxy(request);
        }
        HttpResponse response = request.send();
        if (response.statusCode() != 200) {
            return null;
        }
        FindByKeyword findByKeyword = JSON.parseObject(response.bodyText(), FindByKeyword.class);
        if (!CollectionUtils.isEmpty(findByKeyword.results())) {
            FindByKeyword.TvResult tvResult = findByKeyword.results().getFirst();
            if (Arrays.asList(tvResult.genre_ids()).contains("16")) {
                return tvResult;
            }
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
