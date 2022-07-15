package com.github.nekolr.peashooter.service.impl;

import com.github.nekolr.peashooter.controller.req.datasource.GetDataSourceList;
import com.github.nekolr.peashooter.entity.domain.DataSource;
import com.github.nekolr.peashooter.job.datasource.RssRefreshJobManager;
import com.github.nekolr.peashooter.repository.DataSourceRepository;
import com.github.nekolr.peashooter.service.IDataSourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataSourceServiceImpl implements IDataSourceService {

    private final RssRefreshJobManager jobManager;
    private final DataSourceRepository dataSourceRepository;

    @Override
    public List<DataSource> findAll() {
        return dataSourceRepository.findAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeById(Long id) {
        dataSourceRepository.deleteById(id);
        boolean success = jobManager.removeJob(String.valueOf(id));
        if (!success) {
            throw new RuntimeException("删除任务失败");
        }
    }

    @Override
    public DataSource getById(Long id) {
        return dataSourceRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DataSource save(DataSource dataSource) {
        dataSource = dataSourceRepository.save(dataSource);
        if (Objects.nonNull(dataSource.getId())) {
            jobManager.removeJob(String.valueOf(dataSource.getId()));
        }
        jobManager.addJob(dataSource.getId(), dataSource.getRefreshSeconds());
        return dataSource;
    }

    @Override
    public Page<DataSource> findAllByPage(GetDataSourceList cmd, Pageable pageable) {
        DataSource dataSource = new DataSource();
        String dataSourceName = cmd.dataSourceName();
        if (StringUtils.hasText(dataSourceName)) {
            dataSource.setName(dataSourceName);
        }
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withNullHandler(ExampleMatcher.NullHandler.IGNORE)
                .withMatcher("name", ExampleMatcher.GenericPropertyMatcher.of(ExampleMatcher.StringMatcher.CONTAINING));
        return dataSourceRepository.findAll(Example.of(dataSource, matcher), pageable);
    }
}
