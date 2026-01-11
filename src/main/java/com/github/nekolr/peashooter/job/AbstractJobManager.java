package com.github.nekolr.peashooter.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;

import java.util.Date;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractJobManager {

    private final Scheduler quartzScheduler;

    protected JobDetail createJob(String key, Class<? extends Job> clazz) {
        return JobBuilder.newJob(clazz)
                .withIdentity(this.getJobKey(key))
                .build();
    }

    protected SimpleTrigger createTrigger(String key, Date startDate, int intervalSeconds) {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder
                .simpleSchedule()
                .repeatForever()
                .withIntervalInSeconds(intervalSeconds);
        return TriggerBuilder.newTrigger()
                .withIdentity(this.getTriggerKey(key))
                .startAt(startDate)
                .withSchedule(scheduleBuilder)
                .build();
    }

    protected SimpleTrigger createTrigger(String key, int intervalSeconds) {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder
                .simpleSchedule()
                .repeatForever()
                .withIntervalInSeconds(intervalSeconds);
        return TriggerBuilder.newTrigger()
                .withIdentity(this.getTriggerKey(key))
                .startNow()
                .withSchedule(scheduleBuilder)
                .build();
    }

    protected void schedule(JobDetail job, Trigger trigger) {
        try {
            this.quartzScheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            log.error("创建定时任务失败", e);
        }
    }

    public boolean removeJob(String key) {
        try {
            JobKey jobKey = this.getJobKey(key);
            TriggerKey triggerKey = this.getTriggerKey(key);
            quartzScheduler.pauseJob(jobKey);
            quartzScheduler.unscheduleJob(triggerKey);
            quartzScheduler.deleteJob(jobKey);
            return true;
        } catch (SchedulerException e) {
            log.error("删除任务失败", e);
            return false;
        }
    }

    protected abstract JobKey getJobKey(String key);

    protected abstract TriggerKey getTriggerKey(String key);

}
