package com.github.nekolr.peashooter.api.themoviedb.client;

import com.github.nekolr.peashooter.api.themoviedb.TheMovieDbApi;
import com.github.nekolr.peashooter.api.themoviedb.response.FindAliasTitle;
import com.github.nekolr.peashooter.api.themoviedb.response.FindById;
import com.github.nekolr.peashooter.config.SettingsManager;
import com.github.nekolr.peashooter.util.JacksonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClient;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TheMovieDbClient implements TheMovieDbApi {

    private final SettingsManager settingsManager;
    private final RestClient defaultRestClient;
    private final RestClient proxyRestClient;
    private static final String DEFAULT_LANGUAGE = "zh-CN";
    private static final String DEFAULT_EXTERNAL_SOURCE = "imdb_id";
    private static final String TVDB_EXTERNAL_SOURCE = "tvdb_id";
    private static final String FIND_SERIES_URI_PARAM_API_KEY = "api_key";
    private static final String FIND_SERIES_URI_PARAM_LANGUAGE = "language";
    private static final String FIND_SERIES_URI_PARAM_EXTERNAL_SOURCE = "external_source";

    @Override
    @Retryable(multiplier = 2)
    public Optional<FindById.TvResult> findByImdbId(String imdbId) {
        return this.findById(imdbId, DEFAULT_EXTERNAL_SOURCE);
    }

    @Override
    @Retryable(multiplier = 2)
    public Optional<FindById.TvResult> findByTvdbId(String tvdbId) {
        return this.findById(tvdbId, TVDB_EXTERNAL_SOURCE);
    }

    @Override
    @Retryable(multiplier = 2)
    public List<FindAliasTitle.Title> findAliasTitles(Integer seriesId) {
        String apiKey = settingsManager.get().getTheMovieDb().getApiKey();
        Boolean useProxy = settingsManager.get().getTheMovieDb().getUseProxy();
        String url = THE_MOVIE_DB_HOST + MessageFormat.format(FIND_ALIAS_TITLE_URI, String.valueOf(seriesId));
        if (useProxy) {
            return this.doFindAliasTitles(proxyRestClient, url, apiKey);
        } else {
            return this.doFindAliasTitles(defaultRestClient, url, apiKey);
        }
    }

    private Optional<FindById.TvResult> findById(String id, String externalSource) {
        String apiKey = settingsManager.get().getTheMovieDb().getApiKey();
        Boolean useProxy = settingsManager.get().getTheMovieDb().getUseProxy();
        String url = THE_MOVIE_DB_HOST + MessageFormat.format(FIND_BY_ID_URI, id);
        if (useProxy) {
            return this.doFindById(proxyRestClient, url, apiKey, externalSource);
        } else {
            return this.doFindById(defaultRestClient, url, apiKey, externalSource);
        }
    }

    private Optional<FindById.TvResult> doFindById(RestClient restClient, String url, String apiKey, String externalSource) {
        ResponseEntity<String> response = restClient.get()
                .uri(url, uriBuilder -> uriBuilder
                        .queryParam(FIND_SERIES_URI_PARAM_API_KEY, apiKey)
                        .queryParam(FIND_SERIES_URI_PARAM_LANGUAGE, DEFAULT_LANGUAGE)
                        .queryParam(FIND_SERIES_URI_PARAM_EXTERNAL_SOURCE, externalSource)
                        .build())
                .retrieve()
                .toEntity(String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            return Optional.empty();
        }

        FindById findById = JacksonUtils.tryParse(() ->
                JacksonUtils.getObjectMapper().readValue(response.getBody(), FindById.class));
        if (!CollectionUtils.isEmpty(findById.tv_results())) {
            return Optional.of(findById.tv_results().getFirst());
        }

        return Optional.empty();
    }

    private List<FindAliasTitle.Title> doFindAliasTitles(RestClient restClient, String url, String apiKey) {
        ResponseEntity<String> response = restClient.get()
                .uri(url, uriBuilder -> uriBuilder
                        .queryParam(FIND_SERIES_URI_PARAM_API_KEY, apiKey)
                        .build())
                .retrieve()
                .toEntity(String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            return Collections.emptyList();
        }

        FindAliasTitle findAliasTitle = JacksonUtils.tryParse(() ->
                JacksonUtils.getObjectMapper().readValue(response.getBody(), FindAliasTitle.class));
        if (!CollectionUtils.isEmpty(findAliasTitle.results())) {
            return findAliasTitle.results();
        }

        return Collections.emptyList();
    }
}
