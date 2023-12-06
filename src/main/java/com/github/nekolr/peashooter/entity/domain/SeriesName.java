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
     * 日语标题
     */
    @Column(name = "title_jp")
    private String titleJp;

    /**
     * 简中标题
     */
    @Column(name = "title_zh_cn")
    private String titleZhCN;

    public SeriesName(String titleJp, String titleZhCN) {
        this.titleJp = titleJp;
        this.titleZhCN = titleZhCN;
    }
}
