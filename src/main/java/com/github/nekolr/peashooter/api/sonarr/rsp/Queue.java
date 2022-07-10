package com.github.nekolr.peashooter.api.sonarr.rsp;

public record Queue(Series series, Episode episode, String title, String downloadId) {
    public record Episode(Integer seasonNumber, Integer episodeNumber, Boolean monitored) {}
}
