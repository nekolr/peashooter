package com.github.nekolr.peashooter.controller;

import com.github.nekolr.peashooter.controller.cmd.datasource.GetDataSourceListCmd;
import com.github.nekolr.peashooter.controller.cmd.datasource.TestRegexpCmd;
import com.github.nekolr.peashooter.controller.vo.datasource.ItemTitleVo;
import com.github.nekolr.peashooter.controller.vo.datasource.MatchResultVo;
import com.github.nekolr.peashooter.dto.JsonBean;
import com.github.nekolr.peashooter.entity.domain.DataSource;
import com.github.nekolr.peashooter.service.IDataSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/dataSource")
@RequiredArgsConstructor
public class DataSourceController {

    private final IDataSourceService dataSourceService;

    /**
     * 保存数据源
     */
    @PostMapping("save")
    public JsonBean<Void> save(@RequestBody DataSource dataSource) {
        dataSourceService.save(dataSource);
        return JsonBean.ok();
    }

    /**
     * 删除数据源
     */
    @PostMapping("delete")
    public JsonBean<Void> delete(@RequestParam("id") Long id) {
        dataSourceService.removeById(id);
        return JsonBean.ok();
    }

    /**
     * 刷新数据源的 rss 文件
     */
    @PostMapping("refreshRss")
    public JsonBean<Void> refreshRss(@RequestParam("id") Long id) {
        dataSourceService.refreshRss(id);
        return JsonBean.ok();
    }

    /**
     * 获取数据源列表，带分页
     */
    @PostMapping("getList")
    public JsonBean<Page<DataSource>> getDataSourceList(@RequestBody GetDataSourceListCmd cmd) {
        Pageable pageable = PageRequest.of(cmd.pageNo() - 1, cmd.pageSize());
        return JsonBean.ok(dataSourceService.findAllByPage(cmd, pageable));
    }

    /**
     * 获取全部数据源
     */
    @GetMapping("getAll")
    public JsonBean<List<DataSource>> getAll() {
        return JsonBean.ok(dataSourceService.findAll());
    }

    /**
     * 获取数据源 rss 文件中所有匹配的集标题
     *
     * @param id    数据源 id
     * @param title 要查询的集标题
     */
    @GetMapping("getItemTitleList")
    public JsonBean<List<ItemTitleVo>> getItemTitleList(@RequestParam("id") Long id,
                                                        @RequestParam(value = "title", required = false) String title) {
        return JsonBean.ok(dataSourceService.getItemTitleList(id, title));
    }

    /**
     * 测试匹配器的表达式
     */
    @PostMapping("testRegexp")
    public JsonBean<List<MatchResultVo>> testRegexp(@RequestBody TestRegexpCmd cmd) {
        return JsonBean.ok(dataSourceService.testRegexp(cmd));
    }

}
