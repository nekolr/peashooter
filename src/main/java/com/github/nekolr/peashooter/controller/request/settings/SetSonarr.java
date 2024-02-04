package com.github.nekolr.peashooter.controller.request.settings;

public record SetSonarr(String url, String apiKey, Integer syncSeconds) {
}
