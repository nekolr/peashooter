package com.github.nekolr.peashooter.controller;

import com.github.nekolr.peashooter.controller.req.settings.*;
import com.github.nekolr.peashooter.entity.JsonBean;
import com.github.nekolr.peashooter.service.ISettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("settings")
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

    @PostMapping("qbittorrent")
    public JsonBean<Void> setQbittorrent(@RequestBody SetQbittorrent setting) {
        settingsService.setQbittorrent(setting);
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
}
