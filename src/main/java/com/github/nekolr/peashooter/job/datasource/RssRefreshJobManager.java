package com.github.nekolr.peashooter.job.datasource;

import com.github.nekolr.peashooter.job.AbstractJobManager;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;

import static com.github.nekolr.peashooter.constant.Peashooter.*;

@Slf4j
@Component
public class RssRefreshJobManager extends AbstractJobManager {

    public RssRefreshJobManager(Scheduler scheduler) {
        super(scheduler);
    }

    public void addJob(Long id, Integer intervalSeconds) {
        JobDetail job = this.createJob(String.valueOf(id), RssRefreshJob.class);
        job.getJobDataMap().put(RSS_REFRESH_JOB_PARAM_DATASOURCE_ID, id);
        Trigger trigger = this.createTrigger(String.valueOf(id), intervalSeconds);
        this.schedule(job, trigger);
    }

    @Override
    protected JobKey getJobKey(String key) {
        return JobKey.jobKey(RSS_REFRESH_JOB_NAME_PREFIX + key);
    }

    @Override
    protected TriggerKey getTriggerKey(String key) {
        return TriggerKey.triggerKey(RSS_REFRESH_TRIGGER_NAME_PREFIX + key);
    }
}
