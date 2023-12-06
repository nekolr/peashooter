package com.github.nekolr.peashooter.service;


import com.github.nekolr.peashooter.entity.dto.SeriesNameDto;
import org.springframework.cache.annotation.*;

import java.util.List;

@CacheConfig(cacheNames = "series")
public interface ISonarrService {

    @Cacheable(key = "'all'")
    List<SeriesNameDto> getSeriesNameList();

    @Caching(evict = {@CacheEvict(key = "'all'", beforeInvocation = true)}, cacheable = {@Cacheable(key = "'all'")})
    List<SeriesNameDto> refreshSeriesName();

    Boolean setupAllGroupIndexer();

    @CacheEvict(key = "'all'", beforeInvocation = true)
    void syncSeriesLatest();
}
