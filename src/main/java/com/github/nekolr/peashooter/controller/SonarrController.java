package com.github.nekolr.peashooter.controller;

import com.github.nekolr.peashooter.entity.JsonBean;
import com.github.nekolr.peashooter.entity.SeriesZhCN;
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
    public JsonBean<List<SeriesZhCN>> series() {
        return JsonBean.ok(sonarrService.getSeriesZhCNList());
    }

    @GetMapping("refresh-series")
    public JsonBean<List<SeriesZhCN>> refreshSeries() {
        return JsonBean.ok(sonarrService.refreshSeriesZhCNList());
    }
}
