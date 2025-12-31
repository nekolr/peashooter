package com.github.nekolr.peashooter.job.datasource;


import com.github.nekolr.peashooter.service.RssRefreshJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import static com.github.nekolr.peashooter.constant.Peashooter.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class RssRefreshJob extends QuartzJobBean {

    private final RssRefreshJobService rssRefreshJobService;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        Long datasourceId = (Long) jobDataMap.get(RSS_REFRESH_JOB_PARAM_DATASOURCE_ID);
        rssRefreshJobService.doExecute(datasourceId);
    }
}
