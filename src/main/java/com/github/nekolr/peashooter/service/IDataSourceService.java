package com.github.nekolr.peashooter.service;

import com.github.nekolr.peashooter.controller.cmd.datasource.GetDataSourceListCmd;
import com.github.nekolr.peashooter.controller.cmd.datasource.TestRegexpCmd;
import com.github.nekolr.peashooter.controller.vo.datasource.ItemTitleVo;
import com.github.nekolr.peashooter.controller.vo.datasource.MatchResultVo;
import com.github.nekolr.peashooter.entity.domain.DataSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IDataSourceService {

    /**
     * 获取全部数据源
     */
    List<DataSource> findAll();

    /**
     * 删除数据源（同时会删除定时任务）
     */
    void removeById(Long id);

    /**
     * 保存数据源（同时会刷新定时任务）
     */
    DataSource save(DataSource ds);

    /**
     * 获取数据源列表，带分页
     */
    Page<DataSource> findAllByPage(GetDataSourceListCmd cmd, Pageable pageable);

    /**
     * 刷新数据源的 rss 文件
     */
    boolean refreshRss(Long id);

    /**
     * 获取数据源 rss 文件中所有匹配的集标题
     */
    List<ItemTitleVo> getItemTitleList(Long id, String title);

    /**
     * 测试匹配器的表达式
     */
    List<MatchResultVo> testRegexp(TestRegexpCmd cmd);
}
