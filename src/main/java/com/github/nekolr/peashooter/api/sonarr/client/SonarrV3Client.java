package com.github.nekolr.peashooter.api.sonarr.client;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.nekolr.peashooter.api.sonarr.SonarrV3Api;
import com.github.nekolr.peashooter.api.sonarr.req.AddRssIndexer;
import com.github.nekolr.peashooter.api.sonarr.rsp.Queue;
import com.github.nekolr.peashooter.api.sonarr.rsp.Series;
import com.github.nekolr.peashooter.api.sonarr.rsp.Status;
import com.github.nekolr.peashooter.config.SettingsManager;
import jodd.http.HttpException;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SonarrV3Client implements SonarrV3Api {

    private final SettingsManager settingsManager;

    @Override
    public List<Queue> getQueueList() {
        String apiKey = settingsManager.get().getSonarr().getApiKey();
        HttpRequest request = HttpRequest.get(this.getUrl(GET_QUEUE_LIST_URI));
        request.header(X_API_KEY_HEADER_NAME, apiKey);
        request.query("page", 1);
        request.query("pageSize", 50);
        request.query("includeSeries", true);
        request.query("includeEpisode", true);
        HttpResponse response = request.send();
        if (response.statusCode() != 200)
            return null;
        JSONObject respObj = JSON.parseObject(response.bodyText());
        return JSON.parseArray(respObj.getString("records"), Queue.class);
    }

    @Override
    public Boolean addRssIndexer(AddRssIndexer indexer) {
        String apiKey = settingsManager.get().getSonarr().getApiKey();
        HttpRequest request = HttpRequest.post(this.getUrl(ADD_INDEXER_URI));
        request.header(X_API_KEY_HEADER_NAME, apiKey);
        request.contentTypeJson();
        request.body(JSON.toJSONString(indexer));
        HttpResponse response = request.send();
        if (response.statusCode() == 201) {
            return Boolean.TRUE;
        } else {
            log.info("请求添加 Indexer 失败：{}", response.bodyText());
            return Boolean.FALSE;
        }
    }

    @Override
    public Status getStatus() {
        String apiKey = settingsManager.get().getSonarr().getApiKey();
        HttpRequest request = HttpRequest.get(this.getUrl(GET_STATUS_URI));
        request.header(X_API_KEY_HEADER_NAME, apiKey);
        try {
            HttpResponse response = request.send();
            if (response.statusCode() != 200)
                return null;
            return JSON.parseObject(response.bodyText(), Status.class);
        } catch (Exception e) {
            log.error("get status error: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public List<Series> getSeriesList() {
        String apiKey = settingsManager.get().getSonarr().getApiKey();
        HttpRequest request = HttpRequest.get(this.getUrl(GET_SERIES_LIST_URI));
        request.header(X_API_KEY_HEADER_NAME, apiKey);
        HttpResponse response = request.send();
        if (response.statusCode() != 200)
            return null;
        return JSON.parseArray(response.bodyText(), Series.class);
    }

    @Override
    @Retryable(retryFor = HttpException.class, backoff = @Backoff(multiplier = 1.5))
    public Series getSeries(String id) {
        String apiKey = settingsManager.get().getSonarr().getApiKey();
        String uri = MessageFormat.format(GET_SERIES_URI, id);
        HttpRequest request = HttpRequest.get(this.getUrl(uri));
        request.header(X_API_KEY_HEADER_NAME, apiKey);
        HttpResponse response = request.send();
        if (response.statusCode() != 200)
            return null;
        return JSON.parseObject(response.bodyText(), Series.class);
    }

    private String getUrl(String uri) {
        return settingsManager.get().getSonarr().getUrl() + uri;
    }
}
