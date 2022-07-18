package com.github.nekolr.peashooter.service;


import com.github.nekolr.peashooter.entity.SeriesZhCN;
import org.springframework.cache.annotation.*;

import java.util.List;

@CacheConfig(cacheNames = "series")
public interface ISonarrService {

    void setSeriesZhCN(Long id, SeriesZhCN series);

    boolean hasSeriesZhCN(Long id);

    @Cacheable(key = "'all'")
    List<SeriesZhCN> getSeriesZhCNList();

    @Caching(evict = {@CacheEvict(key = "'all'", beforeInvocation = true)}, cacheable = {@Cacheable(key = "'all'")})
    List<SeriesZhCN> refreshSeriesZhCNList();
}
