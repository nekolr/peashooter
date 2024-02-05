package com.github.nekolr.peashooter.rss.convertor;

/**
 * 匹配器
 *
 * @param regexp        表达式
 * @param offset        表达式开始匹配的偏移量
 * @param season        季度
 * @param episodeOffset 剧集偏移量
 */
public record Matcher(String regexp, Integer offset, Integer season, Integer episodeOffset) {

}
