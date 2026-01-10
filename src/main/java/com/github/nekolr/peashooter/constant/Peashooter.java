package com.github.nekolr.peashooter.constant;

import java.io.File;

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
     * 种子重命名任务执行间隔时间（秒）
     */
    int RENAME_TORRENT_JOB_INTERVAL_SECONDS = 10;

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
     * 原始 RSS 文件目录
     */
    String ORIGINAL_RSS_FILE_DIR = RSS_FILE_HOME_DIR + "original" + File.separator;

    /**
     * 转换后的 RSS 文件目录
     */
    String CONVERTED_RSS_FILE_DIR = RSS_FILE_HOME_DIR + "converted" + File.separator;

    /**
     * 数据源刷新任务名称前缀
     */
    String RSS_REFRESH_JOB_NAME_PREFIX = "RSS_REFRESH_JOB_";

    /**
     * 数据源刷新任务触发器名称前缀
     */
    String RSS_REFRESH_TRIGGER_NAME_PREFIX = "RSS_REFRESH_TRIGGER_";

    /**
     * 数据源刷新任务参数：数据源 ID
     */
    String RSS_REFRESH_JOB_PARAM_DATASOURCE_ID = "PARAM_DATASOURCE_ID";

    /**
     * 种子重命名任务名称
     */
    String RENAME_TORRENT_JOB_NAME = "RENAME_TORRENT_JOB";

    /**
     * 种子重命名任务触发器名称
     */
    String RENAME_TORRENT_TRIGGER_NAME = "RENAME_TORRENT_TRIGGER";

    /**
     * {Series Title} - S{season}E{episode} - {Episode Title} - {Language} - {Quality Full}
     */
    String EPISODE_TITLE_TEMPLATE = "{0} - S{1}E{2} - {3} - {4} - {5}";

    static String getGroupRssFilepath(Long groupId) {
        return CONVERTED_RSS_FILE_DIR + groupId + XML_SUFFIX;
    }

    static String getAutomatedGroupRssFilepath() {
        return CONVERTED_RSS_FILE_DIR + AUTOMATED + XML_SUFFIX;
    }

    static String getDatasourceRssFilepath(Long datasourceId) {
        return ORIGINAL_RSS_FILE_DIR + datasourceId + XML_SUFFIX;
    }

    static String getGroupRssFilepath(String filename) {
        return CONVERTED_RSS_FILE_DIR + filename;
    }

    static String getGroupRssFileUrl(String mappingUrl, Long groupId) {
        return mappingUrl + "/api/group/" + groupId + XML_SUFFIX;
    }

    static String getAutomatedGroupRssFileUrl(String mappingUrl) {
        return mappingUrl + "/api/group/" + AUTOMATED + XML_SUFFIX;
    }

    static String getAllGroupLink(String mappingUrl) {
        return mappingUrl + "/api/group/allRss.xml";
    }
}
