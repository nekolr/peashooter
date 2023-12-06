package com.github.nekolr.peashooter.entity.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "p_series_name")
public class SeriesName {

    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 英文标题
     */
    @Column(name = "title_en")
    private String titleEn;

    /**
     * 简中标题
     */
    @Column(name = "title_zh_cn")
    private String titleZhCN;

    public SeriesName(String titleEn, String titleZhCN) {
        this.titleEn = titleEn;
        this.titleZhCN = titleZhCN;
    }
}
