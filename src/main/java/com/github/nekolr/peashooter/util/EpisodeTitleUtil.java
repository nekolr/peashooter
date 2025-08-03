package com.github.nekolr.peashooter.util;

import com.github.nekolr.peashooter.constant.Peashooter;

import java.text.MessageFormat;

import static com.github.nekolr.peashooter.constant.Peashooter.EPISODE_TITLE_TEMPLATE;

public class EpisodeTitleUtil {

    /**
     * 格式化剧集标题
     */
    public static String formatEpisodeTitle(String seriesTitle, Integer season, String episodeNum, String quality, String language) {
        String epNum = FillUpZeroUtil.fill(episodeNum);
        String epTitle = Peashooter.EPISODE_TITLE_PREFIX + epNum;
        return MessageFormat.format(EPISODE_TITLE_TEMPLATE, seriesTitle, season, epNum, epTitle, language, quality);
    }
}
