package com.github.nekolr.peashooter.service.impl;

import com.github.nekolr.peashooter.entity.domain.GroupDataSource;
import com.github.nekolr.peashooter.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DataSourceRssRefreshJobServiceImpl implements DataSourceRssRefreshJobService {

    private final IGroupService groupService;
    private final IRawParserService rawParserService;
    private final IGroupDataSourceService gdService;
    private final IDataSourceService dataSourceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doExecute(Long datasourceId) {
        log.info("开始执行数据源：{} 的刷新任务", datasourceId);

        boolean refreshed = dataSourceService.refreshRss(datasourceId);
        if (refreshed) {
            log.info("数据源：{} 刷新完毕，开始搜索使用了该数据源的分组", datasourceId);
            List<GroupDataSource> referenceGroups = gdService.getByDatasourceId(datasourceId);
            for (GroupDataSource referenceGroup : referenceGroups) {
                log.info("刷新分组：{} 的转换结果", referenceGroup.getGroupId());
                groupService.refreshRss(referenceGroup.getGroupId());
            }
            log.info("刷新分组完毕，开始执行自动解析转换任务");
            rawParserService.autoParse(datasourceId);
            log.info("刷新任务执行结束");
            return;
        }
        log.info("数据源没有发生变化");
    }
}
