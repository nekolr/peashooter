package com.github.nekolr.peashooter.service;

public interface IRawParserService {

    /**
     * 自动解析所有数据源并全量重建自动分组的 rss
     */
    void autoParseAll();
}
