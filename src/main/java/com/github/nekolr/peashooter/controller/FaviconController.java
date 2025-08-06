package com.github.nekolr.peashooter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FaviconController {

    @GetMapping("/favicon.ico")
    public void favicon() {
        // 返回空响应，避免报错
    }
}
