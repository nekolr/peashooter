package com.github.nekolr.peashooter.controller;

import com.github.nekolr.peashooter.service.IDownloadInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("api/torrents")
@RequiredArgsConstructor
public class TorrentsController {

    private final IDownloadInfoService downloadInfoService;


    @GetMapping
    public void torrent(@RequestParam("title") String title, @RequestParam("episode") Integer episode,
                        @RequestParam("season") Integer season, @RequestParam("series") String series,
                        @RequestParam("url") String url, HttpServletResponse response) {
        downloadInfoService.onDownload(series, title, season, episode);
        try {
            String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);
            if (decodedUrl.startsWith("magnet:")) {
                // magnet 链接无法使用 sendRedirect，直接返回链接内容
                response.setContentType("text/plain");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(decodedUrl);
            } else {
                // .torrent 文件链接，使用重定向
                response.sendRedirect(decodedUrl);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
