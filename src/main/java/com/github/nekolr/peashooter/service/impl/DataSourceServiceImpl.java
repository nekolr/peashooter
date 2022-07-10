package com.github.nekolr.peashooter.service.impl;

import com.github.nekolr.peashooter.controller.req.datasource.AddDataSource;
import com.github.nekolr.peashooter.entity.domain.DataSource;
import com.github.nekolr.peashooter.entity.mapper.DataSourceMapper;
import com.github.nekolr.peashooter.job.datasource.RssRefreshJobManager;
import com.github.nekolr.peashooter.repository.DataSourceRepository;
import com.github.nekolr.peashooter.service.IDataSourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataSourceServiceImpl implements IDataSourceService {

    private final DataSourceMapper dataSourceMapper;
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
    }

    @Override
    public DataSource getById(Long id) {
        return dataSourceRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DataSource save(DataSource dataSource) {
        return dataSourceRepository.save(dataSource);
    }

    @Override
    public Page<DataSource> findAllByPage(DataSource dataSource, Pageable pageable) {
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withNullHandler(ExampleMatcher.NullHandler.IGNORE)
                .withMatcher("name", ExampleMatcher.GenericPropertyMatcher.of(ExampleMatcher.StringMatcher.CONTAINING));
        return dataSourceRepository.findAll(Example.of(dataSource, matcher), pageable);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(AddDataSource addDataSource) {
        DataSource dataSource = dataSourceMapper.toDomain(addDataSource);
        this.save(dataSource);
        jobManager.addJob(dataSource.getId(), dataSource.getRefreshSeconds());
    }
}
