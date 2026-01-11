package com.github.nekolr.peashooter.controller.vo.group;

import com.github.nekolr.peashooter.rss.convertor.Matcher;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class GroupVo {

    /**
     * ID
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 引用 ID
     */
    private String referenceId;

    /**
     * 视频质量
     */
    private String quality;

    /**
     * 语言
     */
    private String language;

    /**
     * 匹配器集合
     */
    private String matchersJson;

    /**
     * RSS 订阅地址
     */
    private String rssLink;

    /**
     * 数据源 id 数组
     */
    private Long[] dataSourceIds;

    /**
     * 匹配器列表
     */
    private List<Matcher> matchers;

}
