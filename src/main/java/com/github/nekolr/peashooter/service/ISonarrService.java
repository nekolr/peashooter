package com.github.nekolr.peashooter.service;


import com.github.nekolr.peashooter.entity.SeriesZhCN;

import java.util.List;

public interface ISonarrService {

    void setSeriesZhCN(Long id, SeriesZhCN series);

    boolean hasSeriesZhCN(Long id);

    List<SeriesZhCN> getSeriesZhCNList();

    List<SeriesZhCN> refreshSeriesZhCNList();
}
