package com.github.nekolr.peashooter.newznab.model;

import java.util.Date;
import java.util.List;

/**
 * Newznab 搜索结果条目
 *
 * @param title         标题
 * @param guid          唯一标识
 * @param pubDate       发布时间，可能为空（为空时输出省略 pubDate）
 * @param enclosureUrl  下载地址（.torrent 或 magnet:），同时作为 item 的 link
 * @param enclosureType enclosure 类型
 * @param size          体积（字节，未知时为 0）
 * @param categories    Newznab 分类 id 列表（规范允许 category 出现多次）
 * @param seriesId      所属剧集 id（Sonarr 剧集 id）
 * @param tvdbId        tvdb id，可能为空
 * @param season        季度，可能为空
 * @param episode       集数，可能为空
 * @param magnetUrl     磁力链接，非磁力时为 null
 * @param infoHash      info hash（40 位小写 hex），非磁力或无法解析时为 null
 */
public record NewznabItem(String title, String guid, Date pubDate,
                          String enclosureUrl, String enclosureType, long size,
                          List<Integer> categories, String seriesId, Long tvdbId,
                          Integer season, Integer episode, String magnetUrl, String infoHash) {

    /**
     * 输出前按 tvdb id 映射补齐（缓存中的条目不含 tvdb id，见 NewznabServiceImpl）
     */
    public NewznabItem withTvdbId(Long tvdbId) {
        return new NewznabItem(title, guid, pubDate, enclosureUrl, enclosureType, size,
                categories, seriesId, tvdbId, season, episode, magnetUrl, infoHash);
    }
}
