package com.github.nekolr.peashooter.api.sonarr.client;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.nekolr.peashooter.api.sonarr.SonarrV3Api;
import com.github.nekolr.peashooter.api.sonarr.req.AddRssIndexer;
import com.github.nekolr.peashooter.api.sonarr.rsp.Queue;
import com.github.nekolr.peashooter.api.sonarr.rsp.Series;
import com.github.nekolr.peashooter.api.sonarr.rsp.Status;
import com.github.nekolr.peashooter.config.SettingsManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.resilience.annotation.Retryable;
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
    public List<Queue> getQueueList() {
        String apiKey = settingsManager.get().getSonarr().getApiKey();

        ResponseEntity<String> response = defaultRestClient.get()
                .uri(settingsManager.get().getSonarr().getUrl() + GET_QUEUE_LIST_URI)
                .header(X_API_KEY_HEADER_NAME, apiKey)
                .retrieve()
                .toEntity(String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            return Collections.emptyList();
        }

        JSONObject respObj = JSON.parseObject(response.getBody());
        return JSON.parseArray(respObj.getString("records"), Queue.class);
    }

    @Override
    public Boolean addRssIndexer(AddRssIndexer indexer) {
        String apiKey = settingsManager.get().getSonarr().getApiKey();

        String requestBody = JSON.toJSONString(indexer);
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

        return JSON.parseObject(response.getBody(), Status.class);
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

        return JSON.parseArray(response.getBody(), Series.class);
    }

    @Override
    @Retryable(multiplier = 1.5)
    public Series getSeries(String id) {
        String apiKey = settingsManager.get().getSonarr().getApiKey();
        String uri = MessageFormat.format(GET_SERIES_URI, id);

        ResponseEntity<String> response = defaultRestClient.get()
                .uri(settingsManager.get().getSonarr().getUrl() + uri)
                .header(X_API_KEY_HEADER_NAME, apiKey)
                .retrieve()
                .toEntity(String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            return null;
        }

        return JSON.parseObject(response.getBody(), Series.class);
    }
}
