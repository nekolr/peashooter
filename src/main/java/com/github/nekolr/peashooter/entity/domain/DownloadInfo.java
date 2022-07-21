package com.github.nekolr.peashooter.entity.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@Table(name = "p_download_info")
@NoArgsConstructor
public class DownloadInfo {

    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 连续剧名称
     */
    private String series;

    /**
     * 集的标题
     */
    private String title;

    /**
     * 季度
     */
    private Integer season;

    /**
     * 集数
     */
    private Integer episode;

    public DownloadInfo(String series, String title, Integer season, Integer episode) {
        this.series = series;
        this.title = title;
        this.season = season;
        this.episode = episode;
    }
}
