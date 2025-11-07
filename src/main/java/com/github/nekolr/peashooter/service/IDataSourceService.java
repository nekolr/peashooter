package com.github.nekolr.peashooter.service;

import com.github.nekolr.peashooter.controller.request.datasource.GetDataSourceList;
import com.github.nekolr.peashooter.controller.request.datasource.TestRegexp;
import com.github.nekolr.peashooter.controller.response.datasource.ItemTitle;
import com.github.nekolr.peashooter.controller.response.datasource.MatchResult;
import com.github.nekolr.peashooter.entity.domain.DataSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IDataSourceService {

    List<DataSource> findAll();

    void removeById(Long id);

    DataSource getById(Long id);

    DataSource save(DataSource ds);

    Page<DataSource> findAllByPage(GetDataSourceList cmd, Pageable pageable);

    boolean refreshRss(Long id);

    List<ItemTitle> getItemTitleList(Long id, String title);

    List<MatchResult> testRegexp(TestRegexp cmd);
}
