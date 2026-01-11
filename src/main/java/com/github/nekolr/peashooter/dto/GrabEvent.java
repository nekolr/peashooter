package com.github.nekolr.peashooter.dto;

import java.util.List;

public record GrabEvent(Series series,
                        List<Episode> episodes,
                        Release release,
                        String downloadClient,
                        String downloadClientType,
                        String downloadId,
                        CustomFormatInfo customFormatInfo,
                        String eventType,
                        String instanceName,
                        String applicationUrl) {

    public record Image(String coverType, String url, String remoteUrl) {
    }

    public record OriginalLanguage(Integer id, String name) {
    }

    public record Series(
            Integer id,
            String title,
            String titleSlug,
            String path,
            Integer tvdbId,
            Integer tvMazeId,
            Integer tmdbId,
            String imdbId,
            String type,
            Integer year,
            List<String> genres,
            List<Image> images,
            List<String> tags,
            OriginalLanguage originalLanguage
    ) {
    }

    public record EpisodeLanguage(Integer id, String name) {
    }

    public record Episode(
            Integer id,
            Integer episodeNumber,
            Integer seasonNumber,
            String title,
            String overview,
            String airDate,
            String airDateUtc,
            Integer seriesId,
            Integer tvdbId
    ) {
    }

    public record Release(
            String quality,
            Integer qualityVersion,
            String releaseTitle,
            String indexer,
            Long size,
            Integer customFormatScore,
            List<String> customFormats,
            List<EpisodeLanguage> languages
    ) {
    }

    public record CustomFormatInfo(
            List<String> customFormats,
            Integer customFormatScore
    ) {
    }
}
