package com.github.nekolr.peashooter.service;

import com.github.nekolr.peashooter.entity.domain.GroupDataSource;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;

import java.util.List;

@CacheConfig(cacheNames = "groupDatasource")
public interface IGroupDataSourceService {

    @CacheEvict(allEntries = true)
    void removeById(Long id);

    GroupDataSource getById(Long id);

    GroupDataSource save(GroupDataSource groupDataSource);

    List<GroupDataSource> getByGroupId(Long groupId);

    List<GroupDataSource> getByDatasourceId(Long datasourceId);
}
