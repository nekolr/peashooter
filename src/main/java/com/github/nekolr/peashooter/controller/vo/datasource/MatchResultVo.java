package com.github.nekolr.peashooter.controller.vo.datasource;

/**
 * 匹配结果
 *
 * @param serialNo    序号
 * @param originTitle 原始标题
 * @param newTitle    格式化后的集标题
 * @param episodeNum  集数
 */
public record MatchResultVo(Integer serialNo, String originTitle, String newTitle, String episodeNum) {
}
