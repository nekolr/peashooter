package com.github.nekolr.peashooter.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static com.github.nekolr.peashooter.constant.Peashooter.CHARSET;

public class GetTorrentLinkUtil {

    private static final String TORRENTS_URI = "/api/torrents";

    /**
     * 格式化种子下载链接
     */
    public static String formatLink(String mappingUrl, String url, String title,
                                  String episode, Integer season, String seriesId) {
        try {
            title = URLEncoder.encode(title, CHARSET);
            return mappingUrl +
                    TORRENTS_URI +
                    "?url=" + url +
                    "&title=" + title +
                    "&episodeNum=" + episode +
                    "&season=" + season +
                    "&series=" + seriesId;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
