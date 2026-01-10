package com.github.nekolr.peashooter.api.sonarr.rsp;

public record Notification(
        Integer id,
        String implementationName,
        String implementation,
        String name
) {
}
