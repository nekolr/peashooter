package com.github.nekolr.peashooter.config;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Settings {

    private Basic basic;
    private Sonarr sonarr;
    private QBitTorrent qbittorrent;
    private HttpProxy httpProxy;
    private TheMovieDb theMovieDb;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Basic {
        private String apiKey;
        private String mappingUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Sonarr {
        private String url;
        private String apiKey;
        private Integer syncSeconds;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QBitTorrent {
        private String url;
        private String username;
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HttpProxy {
        private String ip;
        private Integer port;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TheMovieDb {
        private String apiKey;
        private Boolean useProxy;
    }

}
