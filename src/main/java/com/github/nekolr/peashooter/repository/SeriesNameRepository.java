package com.github.nekolr.peashooter.repository;

import com.github.nekolr.peashooter.entity.domain.SeriesName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;


public interface SeriesNameRepository  extends JpaRepository<SeriesName, Long>, JpaSpecificationExecutor<SeriesName> {

    /**
     * 按英文标题精确查询
     */
    Optional<SeriesName> findByTitleEn(String titleEn);
}
