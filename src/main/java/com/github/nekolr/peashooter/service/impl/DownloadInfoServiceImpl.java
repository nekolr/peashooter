package com.github.nekolr.peashooter.service.impl;

import com.github.nekolr.peashooter.entity.domain.DownloadInfo;
import com.github.nekolr.peashooter.initializer.RenameTorrentJobInitializer;
import com.github.nekolr.peashooter.repository.DownloadInfoRepository;
import com.github.nekolr.peashooter.service.IDownloadInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@CacheConfig(cacheNames = "downloadInfo")
@RequiredArgsConstructor
public class DownloadInfoServiceImpl implements IDownloadInfoService {

    private final RenameTorrentJobInitializer initializer;

    private final DownloadInfoRepository downloadInfoRepository;

    @Override
    @Cacheable(key = "'all'")
    public List<DownloadInfo> findAll() {
        return downloadInfoRepository.findAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {@CacheEvict(key = "'all'"), @CacheEvict(key = "#p0")})
    public void removeById(Long id) {
        downloadInfoRepository.deleteById(id);
    }

    @Override
    @Cacheable(key = "#p0")
    public DownloadInfo getById(Long id) {
        return downloadInfoRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(key = "'all'")
    public DownloadInfo save(DownloadInfo info) {
        return downloadInfoRepository.save(info);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(key = "'all'")
    public void onDownload(String series, String title, Integer season, Integer episode) {
        DownloadInfo info = new DownloadInfo(series, title, season, episode);
        Optional<DownloadInfo> optional = downloadInfoRepository.findOne(Example.of(info));
        if (optional.isEmpty()) {
            this.save(info);
        }
        if (!initializer.isInitialized()) {
            initializer.initRenameTorrentJob();
        }
    }
}
