package com.github.nekolr.peashooter.repository;

import com.github.nekolr.peashooter.entity.domain.DataSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DataSourceRepository  extends JpaRepository<DataSource, Long>, JpaSpecificationExecutor<DataSource> {
}
