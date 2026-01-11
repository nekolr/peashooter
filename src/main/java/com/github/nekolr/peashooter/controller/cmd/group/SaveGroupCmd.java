package com.github.nekolr.peashooter.controller.cmd.group;

import com.github.nekolr.peashooter.rss.convertor.Matcher;

import java.util.List;

/**
 *
 * @param id            分组 id
 * @param name          分组名称
 * @param referenceId   剧集 id
 * @param dataSourceIds 数据源 id 数组
 * @param quality       质量
 * @param language      语言
 * @param matchers      匹配器集合
 */
public record SaveGroupCmd(Long id,
                           String name,
                           String referenceId,
                           Long[] dataSourceIds,
                           String quality,
                           String language,
                           List<Matcher> matchers) {

}
