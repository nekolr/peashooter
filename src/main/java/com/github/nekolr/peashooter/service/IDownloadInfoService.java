package com.github.nekolr.peashooter.service;

import com.github.nekolr.peashooter.entity.domain.DownloadInfo;

import java.util.List;

public interface IDownloadInfoService {

    List<DownloadInfo> findAll();

    void removeById(Long id);

    DownloadInfo getById(Long id);

    DownloadInfo save(DownloadInfo info);

    void onDownload(String series, String title, Integer season, Integer episode);
}
