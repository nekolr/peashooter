package com.github.nekolr.peashooter.controller;

import com.github.nekolr.peashooter.api.sonarr.rsp.Series;
import com.github.nekolr.peashooter.entity.JsonBean;
import com.github.nekolr.peashooter.service.ISonarrService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/sonarr")
@RequiredArgsConstructor
public class SonarrController {

    private final ISonarrService sonarrService;

    @GetMapping("series")
    public JsonBean<List<Series>> series() {
        return JsonBean.ok(sonarrService.getSeriesZhCNList());
    }

    @GetMapping("refresh")
    public JsonBean<List<Series>> refresh() {
        return JsonBean.ok(sonarrService.refreshSeriesZhCNList());
    }
}
