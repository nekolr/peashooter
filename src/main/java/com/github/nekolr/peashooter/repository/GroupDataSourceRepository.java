package com.github.nekolr.peashooter.repository;

import com.github.nekolr.peashooter.entity.domain.GroupDataSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface GroupDataSourceRepository  extends JpaRepository<GroupDataSource, Long>, JpaSpecificationExecutor<GroupDataSource> {

    List<GroupDataSource> findByGroupId(Long groupId);

    List<GroupDataSource> findByDatasourceId(Long datasourceId);
}
