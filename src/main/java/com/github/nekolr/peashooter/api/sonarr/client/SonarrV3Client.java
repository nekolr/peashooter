package com.github.nekolr.peashooter.api.sonarr.client;

import com.alibaba.fastjson2.JSON;
import com.github.nekolr.peashooter.api.sonarr.SonarrV3Api;
import com.github.nekolr.peashooter.api.sonarr.req.AddRssIndexer;
import com.github.nekolr.peashooter.config.SettingsManager;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.github.nekolr.peashooter.constant.Peashooter.API_KEY;

@Slf4j
@Component
@RequiredArgsConstructor
public class SonarrV3Client implements SonarrV3Api {

    private final SettingsManager settingsManager;

    @Override
    public Boolean addRssIndexer(AddRssIndexer indexer) {
        String apiKey = settingsManager.get().getSonarr().getApiKey();
        HttpRequest request = HttpRequest.post(this.getUrl(ADD_INDEXER_URI));
        request.query(API_KEY, apiKey);
        request.body(JSON.toJSONString(indexer));
        HttpResponse response = request.send();
        if (response.statusCode() == 201) {
            return Boolean.TRUE;
        } else {
            log.info("请求添加 Indexer 失败：{}", response.bodyText());
            return Boolean.FALSE;
        }
    }

    private String getUrl(String uri) {
        return settingsManager.get().getSonarr().getUrl() + uri;
    }
}
