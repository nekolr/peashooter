package com.github.nekolr.peashooter.constant;

import java.io.File;

public interface Peashooter {

    /**
     * 常量
     */
    String DOT = ".";
    String AND = "&";
    String API = "api";
    String EQUALS = "=";
    String QUESTION = "?";
    String GROUP = "group";
    String LEFT_SLASH = "/";
    String CHARSET = "utf-8";
    String XML_SUFFIX = "xml";
    String API_KEY = "apiKey";
    String ALL_RSS = "allRss";
    String RSS_2_0 = "rss_2.0";
    String MI_KAN_URL = "https://mikanani.me";

    /**
     * 种子重命名任务执行间隔时间（秒）
     */
    int RENAME_TORRENT_JOB_INTERVAL_SECONDS = 10;

    /**
     * 版本号
     */
    String VERSION = "0.0.1";

    /**
     * 应用名称
     */
    String APPLICATION_NAME = Peashooter.class.getSimpleName();

    /**
     * 正则表达式中，集数的分组名称
     */
    String EPISODE_NUM_GROUP_NAME = "episode";

    /**
     * 集标题前缀
     */
    String EPISODE_TITLE_PREFIX = "Episode ";

    /**
     * RSS 标题
     */
    String RSS_TITLE = APPLICATION_NAME + " " + VERSION;

    /**
     * RSS 描述
     */
    String RSS_DESCRIPTION = "rss convertor";

    /**
     * 配置文件名称
     */
    String SETTINGS_FILE_NAME = "settings.json";

    /**
     * 家目录
     */
    String HOME_DIR = System.getProperty("user.home") + File.separator + "peashooter";

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

    static String getRssFilename(Long id) {
        return id + DOT + XML_SUFFIX;
    }

    static String getRssFilepath(Long id, boolean converted) {
        if (converted) {
            return CONVERTED_RSS_FILE_DIR + getRssFilename(id);
        } else {
            return ORIGINAL_RSS_FILE_DIR + getRssFilename(id);
        }
    }

    static String getRssFilepath(String filename, boolean converted) {
        if (converted) {
            return CONVERTED_RSS_FILE_DIR + filename;
        } else {
            return ORIGINAL_RSS_FILE_DIR + filename;
        }
    }

    static String getGroupLink(String mappingUrl, Long id) {
        return mappingUrl + LEFT_SLASH + API + LEFT_SLASH + GROUP + LEFT_SLASH + id + DOT + XML_SUFFIX;
    }

    static String getAllGroupLink(String mappingUrl) {
        return mappingUrl + LEFT_SLASH + API + LEFT_SLASH + GROUP + LEFT_SLASH + ALL_RSS + DOT + XML_SUFFIX;
    }
}
