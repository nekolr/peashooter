package com.github.nekolr.peashooter.rss;

/**
 * 多媒体内容
 *
 * @param url    地址
 * @param length 大小
 * @param type   MIME 类型
 */
public record Enclosure(String url, Long length, String type) {

}
