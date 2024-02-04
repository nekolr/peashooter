package com.github.nekolr.peashooter.service;

import com.github.nekolr.peashooter.controller.request.datasource.GetDataSourceList;
import com.github.nekolr.peashooter.controller.request.datasource.TestRegexp;
import com.github.nekolr.peashooter.controller.response.datasource.ItemTitle;
import com.github.nekolr.peashooter.controller.response.datasource.MatchResult;
import com.github.nekolr.peashooter.entity.domain.DataSource;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@CacheConfig(cacheNames = "dataSource")
public interface IDataSourceService {

    @Cacheable(key = "'all'")
    List<DataSource> findAll();

    @Caching(evict = {@CacheEvict(key = "'all'"), @CacheEvict(key = "#p0")})
    void removeById(Long id);

    @Cacheable(key = "#p0")
    DataSource getById(Long id);

    @CacheEvict(key = "'all'")
    DataSource save(DataSource ds);

    Page<DataSource> findAllByPage(GetDataSourceList cmd, Pageable pageable);

    @Caching(evict = {@CacheEvict(key = "'all'"), @CacheEvict(key = "#p0")})
    boolean refreshRss(Long id);

    List<ItemTitle> getItemTitleList(Long id);

    List<MatchResult> testRegexp(TestRegexp cmd);
}
