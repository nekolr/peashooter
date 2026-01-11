package com.github.nekolr.peashooter.job.datasource;

import com.github.nekolr.peashooter.job.AbstractJobManager;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;

import static com.github.nekolr.peashooter.constant.Peashooter.*;

@Slf4j
@Component
public class DataSourceRssRefreshJobManager extends AbstractJobManager {

    public DataSourceRssRefreshJobManager(Scheduler scheduler) {
        super(scheduler);
    }

    public void addJob(Long id, Integer intervalSeconds) {
        JobDetail job = this.createJob(String.valueOf(id), DataSourceRssRefreshJob.class);
        job.getJobDataMap().put(DATASOURCE_RSS_REFRESH_JOB_PARAM_DATASOURCE_ID, id);
        Trigger trigger = this.createTrigger(String.valueOf(id), intervalSeconds);
        this.schedule(job, trigger);
    }

    @Override
    protected JobKey getJobKey(String key) {
        return JobKey.jobKey(DATASOURCE_RSS_REFRESH_JOB_NAME_PREFIX + key);
    }

    @Override
    protected TriggerKey getTriggerKey(String key) {
        return TriggerKey.triggerKey(DATASOURCE_RSS_REFRESH_TRIGGER_NAME_PREFIX + key);
    }
}
