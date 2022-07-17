package com.github.nekolr.peashooter.api.sonarr.client;

import com.alibaba.fastjson2.JSON;
import com.github.nekolr.peashooter.api.sonarr.rsp.Series;
import com.github.nekolr.peashooter.config.SettingsManager;
import com.github.nekolr.peashooter.api.sonarr.SonarrApi;
import com.github.nekolr.peashooter.api.sonarr.rsp.Queue;
import com.github.nekolr.peashooter.api.sonarr.rsp.Status;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;

import static com.github.nekolr.peashooter.constant.Peashooter.API_KEY;

@Slf4j
@Component
@RequiredArgsConstructor
public class SonarrClient implements SonarrApi {

    private final SettingsManager settingsManager;

    @Override
    public Status getStatus() {
        String apiKey = settingsManager.get().getSonarr().getApiKey();
        HttpRequest request = HttpRequest.get(this.getUrl(GET_STATUS_URI));
        request.query(API_KEY, apiKey);
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
    public List<Queue> getQueueList() {
        String apiKey = settingsManager.get().getSonarr().getApiKey();
        HttpRequest request = HttpRequest.get(this.getUrl(GET_QUEUE_LIST_URI));
        request.query(API_KEY, apiKey);
        HttpResponse response = request.send();
        if (response.statusCode() != 200)
            return null;
        return JSON.parseArray(response.bodyText(), Queue.class);
    }

    @Override
    public Series getSeries(String id) {
        String apiKey = settingsManager.get().getSonarr().getApiKey();
        String uri = MessageFormat.format(GET_SERIES_URI, id);
        HttpRequest request = HttpRequest.get(this.getUrl(uri));
        request.query(API_KEY, apiKey);
        HttpResponse response = request.send();
        if (response.statusCode() != 200)
            return null;
        return JSON.parseObject(response.bodyText(), Series.class);
    }

    @Override
    public List<Series> getSeriesList() {
        String apiKey = settingsManager.get().getSonarr().getApiKey();
        HttpRequest request = HttpRequest.get(this.getUrl(GET_SERIES_LIST_URI));
        request.query(API_KEY, apiKey);
        HttpResponse response = request.send();
        if (response.statusCode() != 200)
            return null;
        return JSON.parseArray(response.bodyText(), Series.class);
    }

    private String getUrl(String uri) {
        return settingsManager.get().getSonarr().getUrl() + uri;
    }
}
