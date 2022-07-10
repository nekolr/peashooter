package com.github.nekolr.peashooter.job.datasource;

import com.github.nekolr.peashooter.entity.domain.DataSource;
import com.github.nekolr.peashooter.entity.domain.GroupDataSource;
import com.github.nekolr.peashooter.rss.load.RssLoader;
import com.github.nekolr.peashooter.rss.write.RssWriter;
import com.github.nekolr.peashooter.service.IDataSourceService;
import com.github.nekolr.peashooter.service.IGroupDataSourceService;
import com.github.nekolr.peashooter.service.IGroupService;
import com.github.nekolr.peashooter.util.Md5Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static com.github.nekolr.peashooter.constant.Peashooter.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class RssRefreshJob extends QuartzJobBean {
    private final RssWriter rssWriter;
    private final RssLoader rssLoader;
    private final IGroupService groupService;
    private final IDataSourceService dataSourceService;
    private final IGroupDataSourceService groupDataSourceService;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        Long datasourceId = (Long) jobDataMap.get(RSS_REFRESH_JOB_PARAM_DATASOURCE_ID);

        DataSource dataSource = dataSourceService.getById(datasourceId);
        String xml = rssLoader.load(dataSource.getSourceUrl(), dataSource.getUseProxy());
        if (Objects.nonNull(xml)) {
            String sign = Md5Util.md5(xml);
            if (!Objects.equals(sign, dataSource.getSignature())) {
                dataSource.setSignature(sign);
                dataSourceService.save(dataSource);
                rssWriter.write(xml, getRssFilepath(datasourceId, false));
                List<GroupDataSource> referenceGroups = groupDataSourceService.getByDatasourceId(datasourceId);
                for (GroupDataSource referenceGroup : referenceGroups) {
                    groupService.refreshRss(referenceGroup.getGroupId());
                }
            }
        }
    }
}
