package com.github.nekolr.peashooter.constant;

import java.io.File;
import java.util.List;

public interface Peashooter {

    /**
     * 常量
     */
    String CHARSET = "utf-8";
    String XML_SUFFIX = ".xml";
    String RSS_2_0 = "rss_2.0";
    String MI_KAN_URL = "https://mikanani.me";
    String VERSION = "0.0.1";
    String RSS_DESCRIPTION = "rss convertor";
    String AUTOMATED = "automated";
    String APPLICATION_NAME = Peashooter.class.getSimpleName();
    String RSS_TITLE = APPLICATION_NAME + " " + VERSION;
    String ON_GRAB_WEBHOOK_NAME = APPLICATION_NAME + " " + "OnGrab Webhook";

    /**
     * 正则表达式中，集数的分组名称
     */
    String EPISODE_NUM_GROUP_NAME = "episode";

    /**
     * 集标题前缀
     */
    String EPISODE_TITLE_PREFIX = "Episode ";

    /**
     * 配置文件名称
     */
    String SETTINGS_FILE_NAME = "settings.json";

    /**
     * 家目录
     */
    String HOME_DIR = System.getProperty("user.dir") + File.separator + "peashooter";

    /**
     * 配置文件目录
     */
    String SETTINGS_DIR = HOME_DIR + File.separator + "conf" + File.separator;

    /**
     * RSS 文件目录
     */
    String RSS_FILE_HOME_DIR = HOME_DIR + File.separator + "rss" + File.separator;

    /**
     * 数据源 RSS 文件目录
     */
    String DATASOURCE_RSS_FILE_DIR = RSS_FILE_HOME_DIR + "datasource" + File.separator;

    /**
     * 分组的 RSS 文件目录
     */
    String GROUP_RSS_FILE_DIR = RSS_FILE_HOME_DIR + "group" + File.separator;

    /**
     * 数据源刷新任务名称前缀
     */
    String DATASOURCE_RSS_REFRESH_JOB_NAME_PREFIX = "DATASOURCE_RSS_REFRESH_JOB_";

    /**
     * 数据源刷新任务触发器名称前缀
     */
    String DATASOURCE_RSS_REFRESH_TRIGGER_NAME_PREFIX = "DATASOURCE_RSS_REFRESH_TRIGGER_";

    /**
     * 数据源刷新任务参数：数据源 ID
     */
    String DATASOURCE_RSS_REFRESH_JOB_PARAM_DATASOURCE_ID = "DATASOURCE_PARAM_DATASOURCE_ID";

    /**
     * {Series Title} - S{season}E{episodeNum} - {Episode Title} - {Language} - {Quality Full}
     */
    String EPISODE_TITLE_TEMPLATE = "{0} - S{1}E{2} - {3} - {4} - {5}";

    /**
     * Newznab/Torznab 接口路径（Sonarr 索引器的 apiPath）
     */
    String NEWZNAB_API_PATH = "/api/newznab";

    /**
     * api key 参数名，统一使用全小写（与 Newznab 标准一致）
     */
    String API_KEY_PARAM = "apikey";

    /**
     * Newznab 属性命名空间
     */
    String NEWZNAB_ATTR_NAMESPACE = "http://www.newznab.com/DTD/2010/feeds/attributes/";

    /**
     * Torznab 属性命名空间。Sonarr 的 torznab.xsd 以此为 targetNamespace，
     * 且 TorznabRssParser 严格按该命名空间读取属性（不读 newznab 命名空间）
     */
    String TORZNAB_ATTR_NAMESPACE = "http://torznab.com/schemas/2015/feed";

    /**
     * Newznab 协议版本
     */
    String NEWZNAB_API_VERSION = "1.0";

    /**
     * Newznab 分类：电视
     */
    int NEWZNAB_CATEGORY_TV = 5000;

    /**
     * Newznab 分类：电视/WEB-DL
     */
    int NEWZNAB_CATEGORY_TV_WEB_DL = 5010;

    /**
     * Newznab 分类：动漫
     */
    int NEWZNAB_CATEGORY_ANIME = 5070;

    /**
     * 结果条目同时标注的分类：TV、TV/WEB-DL、TV/Anime
     * Newznab 规范允许 category 出现多次，多标注可兼容按不同分类检索的客户端
     */
    List<Integer> NEWZNAB_ITEM_CATEGORIES = List.of(
            NEWZNAB_CATEGORY_TV, NEWZNAB_CATEGORY_TV_WEB_DL, NEWZNAB_CATEGORY_ANIME);

    /**
     * Newznab 搜索结果默认条数
     */
    int NEWZNAB_DEFAULT_LIMIT = 100;

    /**
     * Newznab 搜索结果最大条数
     */
    int NEWZNAB_MAX_LIMIT = 100;

    /**
     * Newznab 条目缓存的有效期（秒），避免每次搜索都全量读盘解析
     */
    int NEWZNAB_ITEMS_CACHE_SECONDS = 60;

    /**
     * 获取分组的 rss 文件地址
     */
    static String getGroupRssFilepath(Long groupId) {
        return GROUP_RSS_FILE_DIR + groupId + XML_SUFFIX;
    }

    /**
     * 获取自动分组的 rss 文件地址
     */
    static String getAutomatedGroupRssFilepath() {
        return GROUP_RSS_FILE_DIR + AUTOMATED + XML_SUFFIX;
    }

    /**
     * 获取数据源的 rss 文件地址
     */
    static String getDatasourceRssFilepath(Long datasourceId) {
        return DATASOURCE_RSS_FILE_DIR + datasourceId + XML_SUFFIX;
    }

    /**
     * 获取分组的 rss 文件地址
     */
    static String getGroupRssFilepath(String filename) {
        return GROUP_RSS_FILE_DIR + filename;
    }

    /**
     * 归一化映射地址：null/blank 返回空串，去掉末尾斜杠。
     * 供各类 URL 拼接方法使用，避免 mappingUrl 为空时拼出 "null/api/..." 这类无效地址
     */
    static String normalizeBaseUrl(String mappingUrl) {
        if (mappingUrl == null || mappingUrl.isBlank()) {
            return "";
        }
        return mappingUrl.endsWith("/")
                ? mappingUrl.substring(0, mappingUrl.length() - 1)
                : mappingUrl;
    }

    /**
     * 获取分组的 rss 文件访问地址
     */
    static String getGroupRssFileUrl(String mappingUrl, Long groupId) {
        return normalizeBaseUrl(mappingUrl) + "/api/group/" + groupId + XML_SUFFIX;
    }

    /**
     * 获取自动分组的 rss 文件访问地址
     */
    static String getAutomatedGroupRssFileUrl(String mappingUrl) {
        return normalizeBaseUrl(mappingUrl) + "/api/group/" + AUTOMATED + XML_SUFFIX;
    }

    /**
     * 获取全部分组的 rss 文件访问地址
     */
    static String getAllGroupRssFileUrl(String mappingUrl) {
        return normalizeBaseUrl(mappingUrl) + "/api/group/allRss.xml";
    }

    /**
     * 获取 Newznab/Torznab 索引器访问地址
     */
    static String getNewznabUrl(String mappingUrl) {
        return normalizeBaseUrl(mappingUrl) + NEWZNAB_API_PATH;
    }
}
