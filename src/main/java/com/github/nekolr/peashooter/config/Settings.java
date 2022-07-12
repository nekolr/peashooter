package com.github.nekolr.peashooter.config;

import lombok.Builder;
import lombok.Data;

@Data
public class Settings {

    private Basic basic;
    private Sonarr sonarr;
    private QBitTorrent qbittorrent;
    private HttpProxy httpProxy;

    @Data
    @Builder
    public static class Basic {
        private String apiKey;
        private String mappingUrl;
    }

    @Data
    @Builder
    public static class Sonarr {
        private String url;
        private String apiKey;
        private Integer syncSeconds;
    }

    @Data
    @Builder
    public static class QBitTorrent {
        private String url;
        private String username;
        private String password;
    }

    @Data
    @Builder
    public static class HttpProxy {
        private String ip;
        private Integer port;
    }

}
