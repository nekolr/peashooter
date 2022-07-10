package com.github.nekolr.peashooter.entity.domain;

import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "p_datasource")
public class DataSource implements Serializable {

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
     * 来源链接
     */
    @Column(name = "source_url")
    private String sourceUrl;

    /**
     * 是否使用代理
     */
    @Column(name = "use_proxy")
    @ColumnDefault("true")
    private Boolean useProxy;

    /**
     * 刷新间隔（单位秒）
     */
    @Column(name = "refresh_seconds")
    private Integer refreshSeconds;

    /**
     * 数据源内容签名
     */
    private String signature;
}
