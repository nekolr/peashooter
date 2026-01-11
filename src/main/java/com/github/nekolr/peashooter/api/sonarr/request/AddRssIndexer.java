package com.github.nekolr.peashooter.api.sonarr.request;

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
        fields.add(new Field("baseUrl", 0, "RSS URL", "textbox", null, false, "normal", false, baseUrl));
    }

    @Data
    @AllArgsConstructor
    public static class Field {
        private String name;
        private Integer order;
        private String label;
        private String type;
        private String unit;
        private Boolean advanced;
        private String privacy;
        private Boolean isFloat;
        private Object value;
    }

    public void setupDefaultFields() {
        fields.add(new Field("cookie", 1, "Cookie", "textbox", null, false, "normal", false, null));
        fields.add(new Field("allowZeroSize", 2, "Allow Zero Size", "checkbox", null, false, "normal", false, true));
        fields.add(new Field("minimumSeeders", 3, "Minimum Seeders", "number", null, true, "normal", false, 1));
        fields.add(new Field("seedCriteria.seedRatio", 4, "Seed Ratio", "textbox", null, false, "normal", false, null));
        fields.add(new Field("seedCriteria.seedTime", 5, "Seed Time", "number", "minutes", true, "normal", false, null));
        fields.add(new Field("seedCriteria.seasonPackSeedTime", 6, "Season-Pack Seed Time", "number", "minutes", true, "normal", false, null));
        fields.add(new Field("rejectBlocklistedTorrentHashesWhileGrabbing", 7, "Reject Blocklisted Torrent Hashes While Grabbing", "checkbox", null, true, "normal", false, false));
    }
}
