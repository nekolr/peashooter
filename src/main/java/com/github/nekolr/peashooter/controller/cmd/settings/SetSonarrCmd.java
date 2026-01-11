package com.github.nekolr.peashooter.controller.cmd.settings;

public record SetSonarrCmd(String url, String apiKey, Integer syncSeconds) {
}
