package com.github.nekolr.peashooter.api.sonarr.response;

import java.util.List;

public record Series(String id, String title, Long tvdbId,
                     String imdbId, String languageProfileId, String qualityProfileId, Boolean monitored,
                     List<Season> seasons) {

    public record Season(Integer seasonNumber, Boolean monitored, Statistics statistics) {

        public record Statistics(Integer totalEpisodeCount) {

        }
    }
}