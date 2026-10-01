package com.github.nekolr.peashooter.api.sonarr.response;

import com.github.nekolr.peashooter.api.sonarr.request.AddRssIndexer;

import java.util.List;

/**
 * Sonarr 索引器（只取需要的字段）
 *
 * @param id             索引器 id
 * @param name           名称
 * @param implementation 实现类型
 * @param fields         字段列表（读取 baseUrl / apiKey 等当前配置）
 */
public record Indexer(Integer id, String name, String implementation, List<AddRssIndexer.Field> fields) {

}
