package com.github.nekolr.peashooter.entity.domain;

import com.github.nekolr.peashooter.rss.convertor.Matcher;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "p_group")
public class Group {

    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 创建时间
     */
    @Column(name = "create_time", insertable = false, updatable = false)
    @ColumnDefault("CURRENT_TIMESTAMP()")
    private Date createTime;

    /**
     * 修改时间
     */
    @Column(name = "update_time")
    private Date updateTime;

    /**
     * 引用 ID
     */
    @Column(name = "reference_id")
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
    @Column(name = "matchers_json", columnDefinition = "text")
    private String matchersJson;

    /**
     * RSS 订阅地址
     */
    @Transient
    private String rssLink;

    /**
     * 数据源
     */
    @Transient
    private Long[] dataSourceIds;

    /**
     * 匹配器
     */
    @Transient
    private List<Matcher> matchers;
}
