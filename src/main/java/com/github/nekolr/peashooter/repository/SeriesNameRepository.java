package com.github.nekolr.peashooter.repository;

import com.github.nekolr.peashooter.entity.domain.SeriesName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface SeriesNameRepository  extends JpaRepository<SeriesName, Long>, JpaSpecificationExecutor<SeriesName> {
}
