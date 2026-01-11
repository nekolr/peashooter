package com.github.nekolr.peashooter.controller;

import com.github.nekolr.peashooter.dto.JsonBean;
import com.github.nekolr.peashooter.dto.SeriesNameDto;
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

    /**
     * 获取所有的剧集名称，按照剧集 id 排序
     */
    @GetMapping("seriesNameList")
    public JsonBean<List<SeriesNameDto>> seriesNameList() {
        return JsonBean.ok(sonarrService.getSeriesNameList());
    }

    /**
     * 刷新所有的剧集名称
     */
    @GetMapping("refreshSeriesNameList")
    public JsonBean<List<SeriesNameDto>> refreshSeriesNameList() {
        return JsonBean.ok(sonarrService.refreshSeriesNameList());
    }

    /**
     * 重新同步剧集名称
     */
    @GetMapping("syncSeriesNameList")
    public JsonBean<Void> syncSeriesNameList() {
        sonarrService.syncSeriesNameList();
        return JsonBean.ok();
    }

    /**
     * 设置索引器（订阅地址为所有分组的 rss 文件地址）
     */
    @PostMapping("setupAllGroupIndexer")
    public JsonBean<Boolean> setupAllGroupIndexer() {
        return JsonBean.ok(sonarrService.setupAllGroupIndexer());
    }
}
