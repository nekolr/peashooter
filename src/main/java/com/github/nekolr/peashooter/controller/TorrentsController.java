package com.github.nekolr.peashooter.controller;

import com.github.nekolr.peashooter.entity.dto.GrabEvent;
import com.github.nekolr.peashooter.service.IDownloadInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("api/torrents")
@RequiredArgsConstructor
public class TorrentsController {

    private final IDownloadInfoService downloadInfoService;

    @PostMapping("/onGrab")
    public void onGrab(@RequestBody GrabEvent grabEvent) {
        Integer seriesId = grabEvent.series().id();
        Integer season = grabEvent.episodes().getFirst().seasonNumber();
        Integer episode = grabEvent.episodes().getFirst().episodeNumber();
        String title = grabEvent.release().releaseTitle();
        downloadInfoService.onDownload(String.valueOf(seriesId), title, season, episode);
    }
}
