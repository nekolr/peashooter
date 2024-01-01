package com.github.nekolr.peashooter.api.sonarr.rsp;

public record Series(String id, String title, Long tvdbId,
                     String imdbId, String languageProfileId, String qualityProfileId) {

}