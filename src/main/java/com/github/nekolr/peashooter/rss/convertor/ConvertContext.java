package com.github.nekolr.peashooter.rss.convertor;

import com.github.nekolr.peashooter.api.sonarr.response.Series;
import lombok.Builder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 进行转换时的上下文
 *
 * @param groupId     分组 id
 * @param referenceId 剧集 id
 * @param quality     质量
 * @param language    语言
 * @param matchers    匹配器列表
 * @param seriesCache 按 referenceId 缓存已查询过的剧集，避免同一次刷新周期内重复调用 Sonarr API
 */
@Builder
public record ConvertContext(Long groupId,
                             String referenceId,
                             String quality,
                             String language,
                             List<Matcher> matchers,
                             Map<String, Series> seriesCache) {

    public ConvertContext {
        if (seriesCache == null) {
            seriesCache = new HashMap<>();
        }
    }
}
