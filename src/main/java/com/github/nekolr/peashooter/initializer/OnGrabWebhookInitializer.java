package com.github.nekolr.peashooter.initializer;

import com.github.nekolr.peashooter.service.ISonarrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OnGrabWebhookInitializer {

    private final ISonarrService sonarrService;

    @EventListener(ApplicationReadyEvent.class)
    public void setupOnGrabWebhook() {
        sonarrService.setupOnGrabWebhook();
    }
}
