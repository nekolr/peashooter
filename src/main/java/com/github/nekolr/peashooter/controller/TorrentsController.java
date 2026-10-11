package com.github.nekolr.peashooter.controller;

import com.github.nekolr.peashooter.api.qb.QBittorrentApi;
import com.github.nekolr.peashooter.dto.GrabEvent;
import com.github.nekolr.peashooter.dto.JsonBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("api/torrents")
@RequiredArgsConstructor
public class TorrentsController {

    private final QBittorrentApi qBittorrentApi;

    /**
     * OnGrab Webhook
     */
    @PostMapping("/onGrab")
    public JsonBean<Void> onGrab(@RequestBody GrabEvent grabEvent) {
        if (!"Test".equals(grabEvent.eventType())) {
            if (Objects.isNull(grabEvent.release()) || !StringUtils.hasText(grabEvent.downloadId())) {
                log.warn("Webhook 事件缺少 release 或 downloadId，忽略: {}", grabEvent.eventType());
                return JsonBean.ok();
            }
            String title = grabEvent.release().releaseTitle();
            String downloadId = grabEvent.downloadId();
            if (!qBittorrentApi.renameTorrent(downloadId, title)) {
                log.warn("Failed to rename torrent {}", title);
            } else {
                log.info("Successfully rename torrent {}: {}", downloadId, title);
            }
        }
        return JsonBean.ok();
    }
}
