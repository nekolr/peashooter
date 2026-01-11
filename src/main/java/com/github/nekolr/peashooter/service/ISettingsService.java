package com.github.nekolr.peashooter.service;

import com.github.nekolr.peashooter.config.Settings;
import com.github.nekolr.peashooter.controller.cmd.settings.*;

public interface ISettingsService {

    /**
     * 测试 sonarr
     */
    boolean testSonarr();

    /**
     * 测试 qBittorrent
     */
    boolean testQb();

    /**
     * 设置基本属性
     */
    void setBasic(SetBasicCmd cmd);

    /**
     * 设置代理
     */
    void setProxy(SetHttpProxyCmd cmd);

    /**
     * 设置 sonarr
     */
    void setSonarr(SetSonarrCmd cmd);

    /**
     * 设置 qBittorrent
     */
    void setQbittorrent(SetQbittorrentCmd cmd);

    /**
     * 刷新 apiKey
     */
    String refreshApiKey();

    /**
     * 获取全部配置
     */
    Settings get();

    /**
     * 设置 themoviedb
     */
    void setTheMovieDb(SetTheMovieDbCmd cmd);
}
