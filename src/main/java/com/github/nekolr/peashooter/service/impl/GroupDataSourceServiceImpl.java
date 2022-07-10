package com.github.nekolr.peashooter.service.impl;

import com.github.nekolr.peashooter.entity.domain.GroupDataSource;
import com.github.nekolr.peashooter.repository.GroupDataSourceRepository;
import com.github.nekolr.peashooter.service.IGroupDataSourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupDataSourceServiceImpl implements IGroupDataSourceService {
    private final GroupDataSourceRepository groupDataSourceRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeById(Long id) {
        groupDataSourceRepository.deleteById(id);
    }

    @Override
    public GroupDataSource getById(Long id) {
        return groupDataSourceRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GroupDataSource save(GroupDataSource groupDataSource) {
        return groupDataSourceRepository.save(groupDataSource);
    }

    @Override
    public List<GroupDataSource> getByGroupId(Long groupId) {
        return groupDataSourceRepository.findByGroupId(groupId);
    }

    @Override
    public List<GroupDataSource> getByDatasourceId(Long datasourceId) {
        return groupDataSourceRepository.findByDatasourceId(datasourceId);
    }
}
