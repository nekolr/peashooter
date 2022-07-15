package com.github.nekolr.peashooter.job.datasource;

import com.github.nekolr.peashooter.entity.domain.GroupDataSource;
import com.github.nekolr.peashooter.service.IDataSourceService;
import com.github.nekolr.peashooter.service.IGroupDataSourceService;
import com.github.nekolr.peashooter.service.IGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.github.nekolr.peashooter.constant.Peashooter.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class RssRefreshJob extends QuartzJobBean {
    private final IGroupService groupService;
    private final IDataSourceService dataSourceService;
    private final IGroupDataSourceService groupDataSourceService;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        Long datasourceId = (Long) jobDataMap.get(RSS_REFRESH_JOB_PARAM_DATASOURCE_ID);

        boolean refreshed = dataSourceService.refreshRss(datasourceId);
        if (refreshed) {
            List<GroupDataSource> referenceGroups = groupDataSourceService.getByDatasourceId(datasourceId);
            for (GroupDataSource referenceGroup : referenceGroups) {
                groupService.refreshRss(referenceGroup.getGroupId());
            }
        }
    }
}
