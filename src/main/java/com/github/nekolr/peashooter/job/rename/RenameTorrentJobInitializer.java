package com.github.nekolr.peashooter.job.rename;

import com.github.nekolr.peashooter.service.ISettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.nekolr.peashooter.constant.Peashooter.RENAME_TORRENT_JOB_INTERVAL_SECONDS;

@Slf4j
@Component
@DependsOn("settingsInitializer")
@RequiredArgsConstructor
public class RenameTorrentJobInitializer implements InitializingBean {

    private final ISettingsService settingsService;
    private final RenameTorrentJobManager jobManager;
    private AtomicBoolean initialized = new AtomicBoolean(false);

    @Override
    public void afterPropertiesSet() {
        if (settingsService.testSonarr() && settingsService.testQb()) {
            this.initRenameTorrentJob();
        }
    }

    public void initRenameTorrentJob() {
        log.info("RenameTorrentJob start");
        jobManager.addJob(RENAME_TORRENT_JOB_INTERVAL_SECONDS);
        this.setInitialized(true);
    }

    public void setInitialized(boolean initialized) {
        this.initialized.getAndSet(initialized);
    }

    public boolean isInitialized() {
        return initialized.get();
    }
}
