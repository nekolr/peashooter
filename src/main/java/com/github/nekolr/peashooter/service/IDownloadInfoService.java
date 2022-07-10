package com.github.nekolr.peashooter.service;

import com.github.nekolr.peashooter.entity.domain.DownloadInfo;
import org.springframework.cache.annotation.*;

import java.util.List;

@CacheConfig(cacheNames = "downloadInfo")
public interface IDownloadInfoService {

    @Cacheable(key = "'all'")
    List<DownloadInfo> findAll();

    @Caching(evict = {@CacheEvict(key = "'all'"), @CacheEvict(key = "#p0")})
    void removeById(Long id);

    @Cacheable(key = "#p0")
    DownloadInfo getById(Long id);

    @CacheEvict(key = "'all'")
    DownloadInfo save(DownloadInfo info);

    @CacheEvict(key = "'all'")
    void onDownload(Long series, String title, Integer season, Integer episode);
}
