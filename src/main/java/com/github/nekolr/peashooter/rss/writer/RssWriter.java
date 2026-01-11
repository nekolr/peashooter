package com.github.nekolr.peashooter.rss.writer;

/**
 * RSS 写入器
 */
public interface RssWriter {

    /**
     * 写入 rss 内容
     *
     * @param xml      rss 文件内容
     * @param filepath 要写入的文件地址
     */
    void write(String xml, String filepath);

}
