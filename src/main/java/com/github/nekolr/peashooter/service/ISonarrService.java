package com.github.nekolr.peashooter.service;


import com.github.nekolr.peashooter.entity.dto.SeriesNameDto;

import java.util.List;

public interface ISonarrService {

    List<SeriesNameDto> getSeriesNameList();

    List<SeriesNameDto> refreshSeriesName();

    Boolean setupAllGroupIndexer();

    void syncSeriesLatest();
}
