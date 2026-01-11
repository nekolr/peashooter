package com.github.nekolr.peashooter.service;


import com.github.nekolr.peashooter.dto.SeriesNameDto;

import java.util.List;

public interface ISonarrService {

    /**
     * 获取所有的剧集名称
     */
    List<SeriesNameDto> getSeriesNameList();

    /**
     * 刷新所有的剧集名称
     */
    List<SeriesNameDto> refreshSeriesNameList();

    /**
     * 设置索引器（订阅地址为所有分组的 rss 文件地址）
     */
    Boolean setupAllGroupIndexer();

    /**
     * 安装 onGrab Webhook
     */
    void setupOnGrabWebhook();

    /**
     * 重新同步剧集名称
     */
    void syncSeriesNameList();
}
