package com.github.nekolr.peashooter.api.sonarr.req;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public class AddRssIndexer {

    private String name;
    private List<Field> fields;
    private Integer priority = 25;
    private Boolean enableRss = true;
    private Boolean supportRss = true;
    private Long downloadClientId = 0L;
    private String protocol = "torrent";
    private Boolean supportSearch = false;
    private Boolean enableAutomaticSearch = false;
    private Boolean enableInteractiveSearch = false;
    private String implementation = "TorrentRssIndexer";
    private String implementationName = "Torrent RSS Feed";
    private String configContract = "TorrentRssIndexerSettings";
    private String infoLink = "https://wiki.servarr.com/sonarr/supported#torrentrssindexer";

    public AddRssIndexer(String name, String baseUrl) {
        this.name = name;
        if (Objects.isNull(fields)) {
            fields = new ArrayList<>();
        }
        fields.add(new Field("baseUrl", baseUrl));
    }

    @Data
    @AllArgsConstructor
    public static class Field {
        private String name;
        private Object value;
    }

    public void setupDefaultFields() {
        fields.add(new Field("cookie", null));
        fields.add(new Field("allowZeroSize", false));
        fields.add(new Field("minimumSeeders", 1));
        fields.add(new Field("eedCriteria.seedRatio", null));
        fields.add(new Field("seedCriteria.seedTime", null));
        fields.add(new Field("seedCriteria.seasonPackSeedTime", null));
    }
}
