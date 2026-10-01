package com.github.nekolr.peashooter.service;

import com.github.nekolr.peashooter.newznab.model.NewznabSearchCriteria;

/**
 * Newznab/Torznab 索引器服务
 */
public interface INewznabService {

    /**
     * 获取能力声明（t=caps）
     */
    String getCaps();

    /**
     * 搜索（t=search / t=tvsearch）
     */
    String search(NewznabSearchCriteria criteria);

    /**
     * 生成错误响应
     */
    String getError(int code, String description);
}
