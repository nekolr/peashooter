package com.github.nekolr.peashooter.entity.domain;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "p_group_datasource")
public class GroupDataSource {

    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 数据源 ID
     */
    @Column(name = "datasource_id")
    private Long datasourceId;

    /**
     * 分组 ID
     */
    @Column(name = "group_id")
    private Long groupId;
}
