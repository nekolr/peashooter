package com.github.nekolr.peashooter.api.sonarr.client;

import com.github.nekolr.peashooter.api.sonarr.SonarrV3Api;
import com.github.nekolr.peashooter.api.sonarr.request.AddWebhookNotification;
import com.github.nekolr.peashooter.api.sonarr.request.AddRssIndexer;
import com.github.nekolr.peashooter.api.sonarr.request.AddTorznabIndexer;
import com.github.nekolr.peashooter.api.sonarr.response.Indexer;
import com.github.nekolr.peashooter.api.sonarr.response.Notification;
import com.github.nekolr.peashooter.api.sonarr.response.Series;
import com.github.nekolr.peashooter.api.sonarr.response.Status;
import com.github.nekolr.peashooter.config.SettingsManager;
import com.github.nekolr.peashooter.util.JacksonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SonarrV3Client implements SonarrV3Api {

    private final SettingsManager settingsManager;
    private final RestClient defaultRestClient;


    @Override
    public Boolean addRssIndexer(AddRssIndexer indexer) {
        String apiKey = settingsManager.get().getSonarr().getApiKey();

        String requestBody = JacksonUtils.tryParse(() ->
                JacksonUtils.getObjectMapper().writeValueAsString(indexer));

        ResponseEntity<String> response = defaultRestClient.post()
                .uri(settingsManager.get().getSonarr().getUrl() + ADD_INDEXER_URI)
                .header(X_API_KEY_HEADER_NAME, apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .toEntity(String.class);

        if (response.getStatusCode() == HttpStatus.CREATED) {
            return Boolean.TRUE;
        } else {
            log.info("请求添加 Indexer 失败：{}", response.getBody());
            return Boolean.FALSE;
        }
    }

    @Override
    public Boolean addTorznabIndexer(AddTorznabIndexer indexer) {
        String apiKey = settingsManager.get().getSonarr().getApiKey();

        String requestBody = JacksonUtils.tryParse(() ->
                JacksonUtils.getObjectMapper().writeValueAsString(indexer));

        ResponseEntity<String> response = defaultRestClient.post()
                .uri(settingsManager.get().getSonarr().getUrl() + ADD_INDEXER_URI)
                .header(X_API_KEY_HEADER_NAME, apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                // 不把 4xx/5xx 转为异常，统一在下方抛出带 status + body 的异常，由全局异常处理返回给调用方
                .onStatus(HttpStatusCode::isError, (request, resp) -> {
                })
                .toEntity(String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return Boolean.TRUE;
        } else {
            throw new IllegalStateException("Sonarr 添加 Torznab 索引器失败：status=" + response.getStatusCode()
                    + "，body=" + response.getBody());
        }
    }

    @Override
    public Boolean updateTorznabIndexer(AddTorznabIndexer indexer) {
        String apiKey = settingsManager.get().getSonarr().getApiKey();

        String requestBody = JacksonUtils.tryParse(() ->
                JacksonUtils.getObjectMapper().writeValueAsString(indexer));

        ResponseEntity<String> response = defaultRestClient.put()
                .uri(settingsManager.get().getSonarr().getUrl() + UPDATE_INDEXER_URI + indexer.getId())
                .header(X_API_KEY_HEADER_NAME, apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                // 不把 4xx/5xx 转为异常，统一在下方抛出带 status + body 的异常，由全局异常处理返回给调用方
                .onStatus(HttpStatusCode::isError, (request, resp) -> {
                })
                .toEntity(String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return Boolean.TRUE;
        } else {
            throw new IllegalStateException("Sonarr 更新 Torznab 索引器失败：status=" + response.getStatusCode()
                    + "，body=" + response.getBody());
        }
    }

    @Override
    public List<Indexer> getIndexers() {
        String apiKey = settingsManager.get().getSonarr().getApiKey();

        ResponseEntity<String> response = defaultRestClient.get()
                .uri(settingsManager.get().getSonarr().getUrl() + GET_INDEXER_URI)
                .header(X_API_KEY_HEADER_NAME, apiKey)
                .retrieve()
                // 不把 4xx/5xx 转为异常，由下方统一处理并带上响应体
                .onStatus(HttpStatusCode::isError, (request, resp) -> {
                })
                .toEntity(String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new IllegalStateException("获取 Sonarr 索引器列表失败：status=" + response.getStatusCode()
                    + "，body=" + response.getBody());
        }

        List<Indexer> indexers = JacksonUtils.tryParse(() ->
                JacksonUtils.getObjectMapper().readValue(response.getBody(),
                        JacksonUtils.getObjectMapper().getTypeFactory().constructCollectionType(List.class, Indexer.class)));
        return indexers == null ? Collections.emptyList() : indexers;
    }

    @Override
    public List<Notification> getNotifications() {
        String apiKey = settingsManager.get().getSonarr().getApiKey();

        ResponseEntity<String> response = defaultRestClient.get()
                .uri(settingsManager.get().getSonarr().getUrl() + GET_NOTIFICATION_URI)
                .header(X_API_KEY_HEADER_NAME, apiKey)
                .retrieve()
                .toEntity(String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            return Collections.emptyList();
        }

        return JacksonUtils.tryParse(() ->
                JacksonUtils.getObjectMapper().readValue(response.getBody(),
                        JacksonUtils.getObjectMapper().getTypeFactory().constructCollectionType(List.class, Notification.class)));
    }

    @Override
    public void addWebhookNotification(AddWebhookNotification notification) {
        String requestBody = JacksonUtils.tryParse(() ->
                JacksonUtils.getObjectMapper().writeValueAsString(notification));

        ResponseEntity<String> response = defaultRestClient.post()
                .uri(settingsManager.get().getSonarr().getUrl() + ADD_NOTIFICATION_URI)
                .header(X_API_KEY_HEADER_NAME, settingsManager.get().getSonarr().getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .toEntity(String.class);

        if (response.getStatusCode() == HttpStatus.CREATED) {
            log.info("成功添加 Sonarr Webhook 通知：{}", notification.getName());
        } else {
            log.info("请求添加 Webhook 通知失败：{}", response.getBody());
        }
    }

    @Override
    public Status getStatus() {
        String apiKey = settingsManager.get().getSonarr().getApiKey();

        ResponseEntity<String> response = defaultRestClient.get()
                .uri(settingsManager.get().getSonarr().getUrl() + GET_STATUS_URI)
                .header(X_API_KEY_HEADER_NAME, apiKey)
                .retrieve()
                .toEntity(String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            return null;
        }

        return JacksonUtils.tryParse(() ->
                JacksonUtils.getObjectMapper().readValue(response.getBody(), Status.class));
    }

    @Override
    public List<Series> getSeriesList() {
        String apiKey = settingsManager.get().getSonarr().getApiKey();
        String url = settingsManager.get().getSonarr().getUrl() + GET_SERIES_LIST_URI;

        ResponseEntity<String> response = defaultRestClient.get()
                .uri(url)
                .header(X_API_KEY_HEADER_NAME, apiKey)
                .retrieve()
                .toEntity(String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            return null;
        }

        return JacksonUtils.tryParse(() ->
                JacksonUtils.getObjectMapper().readValue(response.getBody(),
                        JacksonUtils.getObjectMapper().getTypeFactory().constructCollectionType(List.class, Series.class)));
    }

    @Override
    public Series getSeries(String id) {
        String apiKey = settingsManager.get().getSonarr().getApiKey();
        String uri = MessageFormat.format(GET_SERIES_URI, id);

        return defaultRestClient.get()
                .uri(settingsManager.get().getSonarr().getUrl() + uri)
                .header(X_API_KEY_HEADER_NAME, apiKey)
                .exchange((_, response) -> {
                    if (response.getStatusCode() != HttpStatus.OK) {
                        log.error("Error when fetching series with ID: {}, status code: {}, body: {}",
                                id, response.getStatusCode(), response.bodyTo(String.class));
                        return null;
                    } else {
                        return JacksonUtils.tryParse(() ->
                                JacksonUtils.getObjectMapper().readValue(response.getBody(), Series.class));
                    }
                });
    }
}
