package com.github.nekolr.peashooter.service.impl;

import com.github.nekolr.peashooter.api.qb.QBittorrentApi;
import com.github.nekolr.peashooter.api.sonarr.SonarrV3Api;
import com.github.nekolr.peashooter.api.sonarr.response.Status;
import com.github.nekolr.peashooter.config.Settings;
import com.github.nekolr.peashooter.config.Settings.*;
import com.github.nekolr.peashooter.config.SettingsManager;
import com.github.nekolr.peashooter.controller.cmd.settings.*;
import com.github.nekolr.peashooter.service.ISettingsService;
import com.github.nekolr.peashooter.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SettingsServiceImpl implements ISettingsService {

    private final SonarrV3Api sonarrV3Api;
    private final QBittorrentApi qBittorrentApi;
    private final SettingsManager settingsManager;

    @Override
    public boolean testSonarr() {
        Status status = sonarrV3Api.getStatus();
        return Objects.nonNull(status);
    }

    @Override
    public boolean testQb() {
        return qBittorrentApi.getAppVersion().isPresent();
    }

    @Override
    public void setBasic(SetBasicCmd cmd) {
        Settings settings = settingsManager.get();

        Basic basic = Settings.Basic.builder()
                .mappingUrl(cmd.mappingUrl())
                .apiKey(cmd.apiKey())
                .build();

        // 构造全新的 Settings 整体替换，避免在共享对象上原地修改造成可见性问题
        settingsManager.update(new Settings(basic, settings.getSonarr(), settings.getQbittorrent(),
                settings.getHttpProxy(), settings.getTheMovieDb()));
    }

    @Override
    public void setProxy(SetHttpProxyCmd cmd) {
        Settings settings = settingsManager.get();

        HttpProxy httpProxy = Settings.HttpProxy.builder()
                .ip(cmd.ip())
                .port(cmd.port())
                .build();

        // 构造全新的 Settings 整体替换，避免在共享对象上原地修改造成可见性问题
        settingsManager.update(new Settings(settings.getBasic(), settings.getSonarr(), settings.getQbittorrent(),
                httpProxy, settings.getTheMovieDb()));
    }

    @Override
    public void setSonarr(SetSonarrCmd cmd) {
        Settings settings = settingsManager.get();

        Sonarr sonarr = Settings.Sonarr.builder()
                .url(cmd.url())
                .apiKey(cmd.apiKey())
                .syncSeconds(cmd.syncSeconds())
                .build();

        // 构造全新的 Settings 整体替换，避免在共享对象上原地修改造成可见性问题
        settingsManager.update(new Settings(settings.getBasic(), sonarr, settings.getQbittorrent(),
                settings.getHttpProxy(), settings.getTheMovieDb()));
    }

    @Override
    public void setQbittorrent(SetQbittorrentCmd cmd) {
        Settings settings = settingsManager.get();

        QBitTorrent qBitTorrent = Settings.QBitTorrent.builder()
                .url(cmd.url())
                .username(cmd.username())
                .password(cmd.password())
                .build();

        // 构造全新的 Settings 整体替换，避免在共享对象上原地修改造成可见性问题
        settingsManager.update(new Settings(settings.getBasic(), settings.getSonarr(), qBitTorrent,
                settings.getHttpProxy(), settings.getTheMovieDb()));
    }

    @Override
    public String refreshApiKey() {
        Settings settings = settingsManager.get();
        String apiKey = RandomUtil.generate(32);
        Basic basic = new Basic(apiKey, settings.getBasic().getMappingUrl());
        // 构造全新的 Settings 整体替换，避免在共享对象上原地修改造成可见性问题
        settingsManager.update(new Settings(basic, settings.getSonarr(), settings.getQbittorrent(),
                settings.getHttpProxy(), settings.getTheMovieDb()));
        return apiKey;
    }

    @Override
    public Settings get() {
        return settingsManager.get();
    }

    @Override
    public void setTheMovieDb(SetTheMovieDbCmd cmd) {
        Settings settings = settingsManager.get();

        TheMovieDb theMovieDb = Settings.TheMovieDb.builder()
                .apiKey(cmd.apiKey())
                .useProxy(cmd.useProxy())
                .build();

        // 构造全新的 Settings 整体替换，避免在共享对象上原地修改造成可见性问题
        settingsManager.update(new Settings(settings.getBasic(), settings.getSonarr(), settings.getQbittorrent(),
                settings.getHttpProxy(), theMovieDb));
    }
}
