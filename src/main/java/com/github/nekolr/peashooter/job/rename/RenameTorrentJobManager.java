package com.github.nekolr.peashooter.job.rename;

import com.github.nekolr.peashooter.job.AbstractJobManager;
import com.github.nekolr.peashooter.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;

import java.util.Date;

import static com.github.nekolr.peashooter.constant.Peashooter.RENAME_TORRENT_JOB_NAME;
import static com.github.nekolr.peashooter.constant.Peashooter.RENAME_TORRENT_TRIGGER_NAME;

@Slf4j
@Component
public class RenameTorrentJobManager extends AbstractJobManager {

    public RenameTorrentJobManager(Scheduler scheduler) {
        super(scheduler);
    }

    public void addJob(Integer intervalSeconds) {
        JobDetail job = this.createJob(RENAME_TORRENT_JOB_NAME, RenameTorrentJob.class);
        Date startDate = DateUtils.plus(new Date(), 10);
        Trigger trigger = this.createTrigger(RENAME_TORRENT_TRIGGER_NAME, startDate, intervalSeconds);
        this.schedule(job, trigger);
    }

    @Override
    protected JobKey getJobKey(String key) {
        return JobKey.jobKey(key);
    }

    @Override
    protected TriggerKey getTriggerKey(String key) {
        return TriggerKey.triggerKey(key);
    }
}
