package com.github.nekolr.peashooter.service.impl;

import com.github.nekolr.peashooter.entity.domain.DownloadInfo;
import com.github.nekolr.peashooter.initializer.RenameTorrentJobInitializer;
import com.github.nekolr.peashooter.repository.DownloadInfoRepository;
import com.github.nekolr.peashooter.service.IDownloadInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DownloadInfoServiceImpl implements IDownloadInfoService {

    private final RenameTorrentJobInitializer initializer;

    private final DownloadInfoRepository downloadInfoRepository;

    @Override
    public List<DownloadInfo> findAll() {
        return downloadInfoRepository.findAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeById(Long id) {
        downloadInfoRepository.deleteById(id);
    }

    @Override
    public DownloadInfo getById(Long id) {
        return downloadInfoRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DownloadInfo save(DownloadInfo info) {
        return downloadInfoRepository.save(info);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onDownload(String series, String title, Integer season, Integer episode) {
        DownloadInfo info = new DownloadInfo(series, title, season, episode);
        Optional<DownloadInfo> optional = downloadInfoRepository.findOne(Example.of(info));
        if (!optional.isPresent()) {
            this.save(info);
        }
        if (!initializer.isInitialized()) {
            initializer.initRenameTorrentJob();
        }
    }
}
