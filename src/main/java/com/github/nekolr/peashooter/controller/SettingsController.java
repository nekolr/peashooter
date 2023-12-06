package com.github.nekolr.peashooter.controller;

import com.github.nekolr.peashooter.config.Settings;
import com.github.nekolr.peashooter.controller.req.settings.*;
import com.github.nekolr.peashooter.entity.dto.JsonBean;
import com.github.nekolr.peashooter.service.ISettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final ISettingsService settingsService;

    @PostMapping("basic")
    public JsonBean<Void> setBasic(@RequestBody SetBasic setting) {
        settingsService.setBasic(setting);
        return JsonBean.ok();
    }

    @PostMapping("sonarr")
    public JsonBean<Void> setSonarr(@RequestBody SetSonarr setting) {
        settingsService.setSonarr(setting);
        return JsonBean.ok();
    }

    @PostMapping("proxy")
    public JsonBean<Void> setProxy(@RequestBody SetHttpProxy setting) {
        settingsService.setProxy(setting);
        return JsonBean.ok();
    }

    @PostMapping("qBittorrent")
    public JsonBean<Void> setQbittorrent(@RequestBody SetQbittorrent setting) {
        settingsService.setQbittorrent(setting);
        return JsonBean.ok();
    }

    @PostMapping("themoviedb")
    public JsonBean<Void> setTheMovieDb(@RequestBody SetTheMovieDb setting) {
        settingsService.setTheMovieDb(setting);
        return JsonBean.ok();
    }

    @GetMapping("test-sonarr")
    public JsonBean<Boolean> testSonarr() {
        return JsonBean.ok(settingsService.testSonarr());
    }

    @GetMapping("test-qb")
    public JsonBean<Boolean> testQb() {
        return JsonBean.ok(settingsService.testQb());
    }

    @PostMapping("basic/refreshApiKey")
    public JsonBean<String> refreshApiKey() {
        return JsonBean.ok(settingsService.refreshApiKey());
    }

    @GetMapping("get")
    public JsonBean<Settings> get() {
        return JsonBean.ok(settingsService.get());
    }
}
