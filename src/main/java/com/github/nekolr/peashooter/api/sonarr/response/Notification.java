package com.github.nekolr.peashooter.api.sonarr.response;

public record Notification(
        Integer id,
        String implementationName,
        String implementation,
        String name
) {
}
