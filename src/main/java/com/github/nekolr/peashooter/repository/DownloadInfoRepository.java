package com.github.nekolr.peashooter.repository;

import com.github.nekolr.peashooter.entity.domain.DownloadInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DownloadInfoRepository extends JpaRepository<DownloadInfo, Long>, JpaSpecificationExecutor<DownloadInfo> {
}
