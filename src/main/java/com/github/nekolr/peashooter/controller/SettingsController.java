package com.github.nekolr.peashooter.controller;

import com.github.nekolr.peashooter.config.Settings;
import com.github.nekolr.peashooter.controller.cmd.settings.*;
import com.github.nekolr.peashooter.dto.JsonBean;
import com.github.nekolr.peashooter.service.ISettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final ISettingsService settingsService;

    /**
     * 设置基本属性
     */
    @PostMapping("basic")
    public JsonBean<Void> setBasic(@RequestBody SetBasicCmd cmd) {
        settingsService.setBasic(cmd);
        return JsonBean.ok();
    }

    /**
     * 设置 sonarr
     */
    @PostMapping("sonarr")
    public JsonBean<Void> setSonarr(@RequestBody SetSonarrCmd cmd) {
        settingsService.setSonarr(cmd);
        return JsonBean.ok();
    }

    /**
     * 设置代理
     */
    @PostMapping("proxy")
    public JsonBean<Void> setProxy(@RequestBody SetHttpProxyCmd cmd) {
        settingsService.setProxy(cmd);
        return JsonBean.ok();
    }

    /**
     * 设置 qBittorrent
     */
    @PostMapping("qBittorrent")
    public JsonBean<Void> setQbittorrent(@RequestBody SetQbittorrentCmd cmd) {
        settingsService.setQbittorrent(cmd);
        return JsonBean.ok();
    }

    /**
     * 设置 themoviedb
     */
    @PostMapping("themoviedb")
    public JsonBean<Void> setTheMovieDb(@RequestBody SetTheMovieDbCmd cmd) {
        settingsService.setTheMovieDb(cmd);
        return JsonBean.ok();
    }

    /**
     * 测试 sonarr
     */
    @GetMapping("test-sonarr")
    public JsonBean<Boolean> testSonarr() {
        return JsonBean.ok(settingsService.testSonarr());
    }

    /**
     * 测试 qBittorrent
     */
    @GetMapping("test-qb")
    public JsonBean<Boolean> testQb() {
        return JsonBean.ok(settingsService.testQb());
    }

    /**
     * 刷新 apiKey
     */
    @PostMapping("basic/refreshApiKey")
    public JsonBean<String> refreshApiKey() {
        return JsonBean.ok(settingsService.refreshApiKey());
    }

    /**
     * 获取所有配置
     */
    @GetMapping("get")
    public JsonBean<Settings> get() {
        return JsonBean.ok(settingsService.get());
    }
}
