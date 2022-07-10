package com.github.nekolr.peashooter.controller.req.settings;

public record SetSonarr(String url, String apiKey, Integer syncSeconds) {
}
