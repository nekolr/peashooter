package com.github.nekolr.peashooter.rss;

import java.util.Date;

/**
 * Rss Item Element
 *
 * @param title      标题
 * @param link       链接
 * @param pubDate    发布时间
 * @param guid       GUID
 * @param enclosure  enclosure
 * @param seriesId   剧集 id
 * @param seasonNum  季度
 * @param episodeNum 集数
 * @param source     源
 */
public record Item(String title, String link, Date pubDate, String guid, Enclosure enclosure, String seriesId,
                   int seasonNum, int episodeNum, String source) {

}
