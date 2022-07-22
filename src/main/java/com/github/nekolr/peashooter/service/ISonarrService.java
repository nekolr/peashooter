package com.github.nekolr.peashooter.service;


import com.github.nekolr.peashooter.entity.SeriesZhCN;
import org.springframework.cache.annotation.*;

import java.util.List;

@CacheConfig(cacheNames = "series")
public interface ISonarrService {

    void setSeriesZhCN(String id, SeriesZhCN series);

    boolean hasSeriesZhCN(String id);

    @Cacheable(key = "'all'")
    List<SeriesZhCN> getSeriesZhCNList();

    @Caching(evict = {@CacheEvict(key = "'all'", beforeInvocation = true)}, cacheable = {@Cacheable(key = "'all'")})
    List<SeriesZhCN> refreshSeriesZhCNList();

    Boolean setupAllGroupIndexer();
}
