package com.github.nekolr.peashooter.controller;

import com.github.nekolr.peashooter.entity.dto.JsonBean;
import com.github.nekolr.peashooter.entity.dto.SeriesNameDto;
import com.github.nekolr.peashooter.service.ISonarrService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/sonarr")
@RequiredArgsConstructor
public class SonarrController {

    private final ISonarrService sonarrService;

    @GetMapping("series")
    public JsonBean<List<SeriesNameDto>> series() {
        return JsonBean.ok(sonarrService.getSeriesNameList());
    }

    @GetMapping("refresh-series")
    public JsonBean<List<SeriesNameDto>> refreshSeries() {
        return JsonBean.ok(sonarrService.refreshSeriesName());
    }

    @GetMapping("refresh-series-fully")
    public JsonBean<Void> refreshSeriesFully() {
        sonarrService.refreshSeriesFully();
        return JsonBean.ok();
    }

    @PostMapping("setupAllGroupIndexer")
    public JsonBean<Void> setupAllGroupIndexer() {
        return JsonBean.ok(sonarrService.setupAllGroupIndexer());
    }
}
