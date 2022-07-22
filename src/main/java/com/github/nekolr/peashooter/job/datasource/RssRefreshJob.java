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
    private final IGroupDataSourceService gdService;
    private final IDataSourceService dataSourceService;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        Long datasourceId = (Long) jobDataMap.get(RSS_REFRESH_JOB_PARAM_DATASOURCE_ID);
        log.info("开始执行数据源：{} 的刷新任务", datasourceId);

        boolean refreshed = dataSourceService.refreshRss(datasourceId);
        if (refreshed) {
            log.info("数据源：{} 刷新完毕，开始搜索使用了该数据源的分组", datasourceId);
            List<GroupDataSource> referenceGroups = gdService.getByDatasourceId(datasourceId);
            for (GroupDataSource referenceGroup : referenceGroups) {
                log.info("刷新分组：{} 的转换结果", referenceGroup.getGroupId());
                groupService.refreshRss(referenceGroup.getGroupId());
            }
            log.info("刷新任务执行结束");
            return;
        }
        log.info("数据源没有发生变化");
    }
}
