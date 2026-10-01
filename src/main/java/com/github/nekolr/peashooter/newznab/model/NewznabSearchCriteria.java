package com.github.nekolr.peashooter.newznab.model;

import java.util.List;

/**
 * Newznab 搜索条件
 *
 * @param t      函数类型（search / tvsearch）
 * @param cat    分类列表
 * @param q      搜索关键字
 * @param tvdbid tvdb id
 * @param imdbid imdb id
 * @param season 季度
 * @param ep     集数
 * @param limit  返回条数上限
 * @param offset 偏移量
 */
public record NewznabSearchCriteria(String t, List<Integer> cat, String q, String tvdbid, String imdbid,
                                    Integer season, Integer ep, int limit, int offset) {

}
