package com.github.nekolr.peashooter.controller.cmd.datasource;

import com.github.nekolr.peashooter.rss.convertor.Matcher;

/**
 * 测试匹配器的表达式
 *
 * @param matcher       匹配器
 * @param dataSourceIds 数据源 id 数组
 * @param seriesTitle   剧集标题
 * @param quality       质量
 * @param language      语言
 */
public record TestRegexpCmd(Matcher matcher, String[] dataSourceIds, String seriesTitle, String quality, String language) {

}
