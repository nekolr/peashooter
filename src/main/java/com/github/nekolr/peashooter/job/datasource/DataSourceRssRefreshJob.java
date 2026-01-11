package com.github.nekolr.peashooter.job.datasource;


import com.github.nekolr.peashooter.service.DataSourceRssRefreshJobService;
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
public class DataSourceRssRefreshJob extends QuartzJobBean {

    private final DataSourceRssRefreshJobService dataSourceRssRefreshJobService;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        Long datasourceId = (Long) jobDataMap.get(DATASOURCE_RSS_REFRESH_JOB_PARAM_DATASOURCE_ID);
        dataSourceRssRefreshJobService.doExecute(datasourceId);
    }
}
