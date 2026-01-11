package com.github.nekolr.peashooter.rss.loader;

/**
 * RSS 加载器
 */
public interface RssLoader {

    /**
     * 从 url 读取 rss
     *
     * @param url      远程 rss 地址
     * @param useProxy 是否使用代理
     * @return rss 内容
     */
    String load(String url, boolean useProxy);

    /**
     * 从文件读取 rss 内容
     *
     * @param filepath rss 文件地址
     * @return rss 文件内容
     */
    String loadFromFile(String filepath);
}
