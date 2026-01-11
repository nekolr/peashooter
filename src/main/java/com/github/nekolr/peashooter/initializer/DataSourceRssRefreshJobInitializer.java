package com.github.nekolr.peashooter.initializer;


import com.github.nekolr.peashooter.entity.domain.DataSource;
import com.github.nekolr.peashooter.job.datasource.DataSourceRssRefreshJobManager;
import com.github.nekolr.peashooter.service.IDataSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@DependsOn("settingsInitializer")
@RequiredArgsConstructor
public class DataSourceRssRefreshJobInitializer implements InitializingBean {

    private final IDataSourceService dataSourceService;
    private final DataSourceRssRefreshJobManager jobManager;

    @Override
    public void afterPropertiesSet() {
        this.initDataSourceRssRefreshJobs();
    }

    private void initDataSourceRssRefreshJobs() {
        List<DataSource> dataSourceList = dataSourceService.findAll();
        dataSourceList.stream()
                .filter(dataSource -> dataSource.getRefreshSeconds() > 0)
                .forEach(dataSource -> jobManager.addJob(dataSource.getId(), dataSource.getRefreshSeconds()));
    }
}
