package com.github.nekolr.peashooter.api.sonarr.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import static com.github.nekolr.peashooter.constant.Peashooter.NEWZNAB_CATEGORY_ANIME;
import static com.github.nekolr.peashooter.constant.Peashooter.NEWZNAB_CATEGORY_TV_WEB_DL;
import static com.github.nekolr.peashooter.constant.Peashooter.NEWZNAB_ITEM_CATEGORIES;

/**
 * 向 Sonarr 添加 Torznab 索引器（Torznab = Newznab 标准 + 种子属性）
 */
@Data
public class AddTorznabIndexer {

    /**
     * 索引器 id，更新已有索引器（PUT）时使用，新建时为 null
     * <p>
     * 为 null 时必须整体省略该字段：Sonarr 的 IndexerResource.Id 是不可空的 int，
     * ASP.NET Core 的 System.Text.Json 无法将 {@code {"id":null}} 反序列化为 int，
     * 会直接返回 400（{@code $.id: The JSON value could not be converted to System.Int32}）。
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer id;
    private String name;
    private List<AddRssIndexer.Field> fields = new ArrayList<>();
    private Integer priority = 25;
    private Boolean enableRss = true;
    private Boolean supportsRss = true;
    private Boolean supportsSearch = true;
    private Boolean enableAutomaticSearch = true;
    private Boolean enableInteractiveSearch = true;
    private Long downloadClientId = 0L;
    private String protocol = "torrent";
    private String implementation = "Torznab";
    private String implementationName = "Torznab";
    private String configContract = "TorznabSettings";
    private String infoLink = "https://wiki.servarr.com/sonarr/supported#torznab";

    public AddTorznabIndexer(String name, String baseUrl, String apiPath, String apiKey) {
        this.name = name;
        fields.add(new AddRssIndexer.Field("baseUrl", 0, "URL", "textbox", null, false, "normal", false, baseUrl));
        fields.add(new AddRssIndexer.Field("apiPath", 1, "API Path", "textbox", null, false, "normal", false, apiPath));
        fields.add(new AddRssIndexer.Field("apiKey", 2, "API Key", "textbox", null, false, "normal", false, apiKey));
    }

    public void setupDefaultFields() {
        fields.add(new AddRssIndexer.Field("categories", 3, "Categories", "select", null, false, "normal", false,
                NEWZNAB_ITEM_CATEGORIES));
        fields.add(new AddRssIndexer.Field("animeCategories", 4, "Anime Categories", "select", null, false, "normal", false,
                List.of(NEWZNAB_CATEGORY_TV_WEB_DL, NEWZNAB_CATEGORY_ANIME)));
        // 转换后的标题按季/集编号（SxxExx），开启后 Sonarr 会使用 season/ep 搜索
        fields.add(new AddRssIndexer.Field("animeStandardFormatSearch", 5, "Anime Standard Format Search", "checkbox",
                null, false, "normal", false, true));
        fields.add(new AddRssIndexer.Field("additionalParameters", 6, "Additional Parameters", "textbox", null, false,
                "normal", false, null));
        fields.add(new AddRssIndexer.Field("minimumSeeders", 7, "Minimum Seeders", "number", null, true, "normal", false, 1));
        fields.add(new AddRssIndexer.Field("seedCriteria.seedRatio", 8, "Seed Ratio", "textbox", null, false, "normal", false, null));
        fields.add(new AddRssIndexer.Field("seedCriteria.seedTime", 9, "Seed Time", "number", "minutes", true, "normal", false, null));
        fields.add(new AddRssIndexer.Field("seedCriteria.seasonPackSeedTime", 10, "Season-Pack Seed Time", "number",
                "minutes", true, "normal", false, null));
    }
}
