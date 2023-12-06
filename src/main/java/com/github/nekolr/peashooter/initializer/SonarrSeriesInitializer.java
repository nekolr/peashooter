package com.github.nekolr.peashooter.initializer;

import com.github.nekolr.peashooter.config.SettingsManager;
import com.github.nekolr.peashooter.service.ISonarrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class SonarrSeriesInitializer {
    private final ISonarrService sonarrService;
    private final SettingsManager settingsManager;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        initSonarrSeries();
    }

    private void initSonarrSeries() {
        String apiKey = settingsManager.get().getTheMovieDb().getApiKey();
        if (!StringUtils.hasText(apiKey)) {
            log.warn("没有配置 TheMovieDb 的 ApiKey，初始化任务终止");
        } else {
            sonarrService.refreshSeriesName();
        }
    }
}
