package com.github.nekolr.peashooter.api.sonarr.rsp;

public record Series(Long id, String title, Integer seasonCount, Long tvdbId,
                     String imdbId, String languageProfileId, String qualityProfileId) {

}