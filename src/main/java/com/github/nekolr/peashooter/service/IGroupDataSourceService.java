package com.github.nekolr.peashooter.service;

import com.github.nekolr.peashooter.entity.domain.GroupDataSource;

import java.util.List;

public interface IGroupDataSourceService {

    void removeById(Long id);

    GroupDataSource getById(Long id);

    GroupDataSource save(GroupDataSource groupDataSource);

    List<GroupDataSource> getByGroupId(Long groupId);

    List<GroupDataSource> getByDatasourceId(Long datasourceId);

    void deleteList(List<GroupDataSource> gds);
}
