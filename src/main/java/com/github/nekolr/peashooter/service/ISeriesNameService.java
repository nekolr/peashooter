package com.github.nekolr.peashooter.service;

import com.github.nekolr.peashooter.entity.domain.SeriesName;

import java.util.List;

public interface ISeriesNameService {

    List<SeriesName> findAll();

    void saveSeriesName(SeriesName seriesName);

    SeriesName findByTitleEn(String titleEn);
}
