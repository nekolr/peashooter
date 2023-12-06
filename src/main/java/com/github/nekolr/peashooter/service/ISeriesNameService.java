package com.github.nekolr.peashooter.service;

import com.github.nekolr.peashooter.entity.domain.SeriesName;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import java.util.List;

@CacheConfig(cacheNames = "seriesName")
public interface ISeriesNameService {

    List<SeriesName> findAll();

    @Caching(evict = {@CacheEvict(key = "#p0.titleEn", condition = "#p0.titleEn != null")})
    void saveSeriesName(SeriesName seriesName);

    @Cacheable(key = "#p0", unless = "null == #result")
    SeriesName findByTitleEn(String titleEn);
}
