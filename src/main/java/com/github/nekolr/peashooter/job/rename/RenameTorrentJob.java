package com.github.nekolr.peashooter.job.rename;

import com.github.nekolr.peashooter.api.qb.QBittorrentApi;
import com.github.nekolr.peashooter.api.sonarr.SonarrV3Api;
import com.github.nekolr.peashooter.api.sonarr.rsp.Queue;
import com.github.nekolr.peashooter.entity.domain.DownloadInfo;
import com.github.nekolr.peashooter.service.IDownloadInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class RenameTorrentJob extends QuartzJobBean {

    private final SonarrV3Api sonarrV3Api;
    private final QBittorrentApi qBittorrentApi;
    private final IDownloadInfoService downloadInfoService;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        List<DownloadInfo> downloadInfoList = downloadInfoService.findAll();
        List<Queue> queueList = sonarrV3Api.getQueueList();
        for (DownloadInfo info : downloadInfoList) {
            if (!CollectionUtils.isEmpty(queueList)) {
                for (Queue queue : queueList) {
                    String seriesId = queue.series().id();
                    if (Objects.equals(seriesId, info.getSeries())) {
                        if (Objects.equals(queue.episode().seasonNumber(), info.getSeason())) {
                            if (Objects.equals(queue.episode().episodeNumber(), info.getEpisode())) {
                                String hash = queue.downloadId();
                                if (qBittorrentApi.renameTorrent(hash, info.getTitle())) {
                                    log.info("rename torrent success, hash: {}, title: {}", hash, info.getTitle());
                                    downloadInfoService.removeById(info.getId());
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
