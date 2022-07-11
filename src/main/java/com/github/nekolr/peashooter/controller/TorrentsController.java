package com.github.nekolr.peashooter.controller;

import com.github.nekolr.peashooter.service.IDownloadInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("api/torrents")
@RequiredArgsConstructor
public class TorrentsController {

    private final IDownloadInfoService downloadInfoService;


    @GetMapping
    public void torrent(@RequestParam("title") String title, @RequestParam("episode") Integer episode,
                        @RequestParam("season") Integer season, @RequestParam("series") Long series,
                        @RequestParam("url") String url, HttpServletResponse response) {
        downloadInfoService.onDownload(series, title, season, episode);
        try {
            response.sendRedirect(url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
