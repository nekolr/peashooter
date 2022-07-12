package com.github.nekolr.peashooter.service.impl;

import com.github.nekolr.peashooter.api.qb.QBittorrentApi;
import com.github.nekolr.peashooter.api.qb.rsp.AppVersion;
import com.github.nekolr.peashooter.api.sonarr.SonarrApi;
import com.github.nekolr.peashooter.api.sonarr.rsp.Status;
import com.github.nekolr.peashooter.config.Settings;
import com.github.nekolr.peashooter.config.Settings.*;
import com.github.nekolr.peashooter.config.SettingsManager;
import com.github.nekolr.peashooter.controller.req.settings.*;
import com.github.nekolr.peashooter.service.ISettingsService;
import com.github.nekolr.peashooter.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SettingsServiceImpl implements ISettingsService {

    private final SonarrApi sonarrApi;
    private final QBittorrentApi qBittorrentApi;
    private final SettingsManager settingsManager;

    @Override
    public boolean testSonarr() {
        Status status = sonarrApi.getStatus();
        return Objects.nonNull(status);
    }

    @Override
    public boolean testQb() {
        AppVersion appVersion = qBittorrentApi.getAppVersion();
        return Objects.nonNull(appVersion);
    }

    @Override
    public void setBasic(SetBasic setting) {
        Settings settings = settingsManager.get();

        Basic basic = Settings.Basic.builder()
                .mappingUrl(setting.mappingUrl())
                .build();

        settings.setBasic(basic);
        settingsManager.update(settings);
    }

    @Override
    public void setProxy(SetHttpProxy setting) {
        Settings settings = settingsManager.get();

        HttpProxy httpProxy = Settings.HttpProxy.builder()
                .ip(setting.ip())
                .port(setting.port())
                .build();

        settings.setHttpProxy(httpProxy);
        settingsManager.update(settings);
    }

    @Override
    public void setSonarr(SetSonarr setting) {
        Settings settings = settingsManager.get();

        Sonarr sonarr = Settings.Sonarr.builder()
                .url(setting.url())
                .apiKey(setting.apiKey())
                .syncSeconds(setting.syncSeconds())
                .build();

        settings.setSonarr(sonarr);
        settingsManager.update(settings);
    }

    @Override
    public void setQbittorrent(SetQbittorrent setting) {
        Settings settings = settingsManager.get();

        QBitTorrent qBitTorrent = Settings.QBitTorrent.builder()
                .url(setting.url())
                .username(setting.username())
                .password(setting.password())
                .build();

        settings.setQbittorrent(qBitTorrent);
        settingsManager.update(settings);
    }

    @Override
    public String refreshApiKey() {
        Settings settings = settingsManager.get();
        String apiKey = RandomUtil.generate(32);
        settings.getBasic().setApiKey(apiKey);
        settingsManager.update(settings);
        return apiKey;
    }
}
