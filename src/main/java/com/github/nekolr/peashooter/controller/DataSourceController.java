package com.github.nekolr.peashooter.controller;

import com.github.nekolr.peashooter.controller.request.datasource.TestRegexp;
import com.github.nekolr.peashooter.controller.request.datasource.GetDataSourceList;
import com.github.nekolr.peashooter.controller.response.datasource.ItemTitle;
import com.github.nekolr.peashooter.controller.response.datasource.MatchResult;
import com.github.nekolr.peashooter.entity.dto.JsonBean;
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

    @PostMapping("save")
    public JsonBean<Void> save(@RequestBody DataSource dataSource) {
        dataSourceService.save(dataSource);
        return JsonBean.ok();
    }

    @PostMapping("delete")
    public JsonBean<Void> delete(@RequestParam("id") Long id) {
        dataSourceService.removeById(id);
        return JsonBean.ok();
    }

    @PostMapping("refreshRss")
    public JsonBean<Void> refreshRss(@RequestParam("id") Long id) {
        dataSourceService.refreshRss(id);
        return JsonBean.ok();
    }

    @PostMapping("getList")
    public JsonBean<Page<DataSource>> getDataSourceList(@RequestBody GetDataSourceList cmd) {
        Pageable pageable = PageRequest.of(cmd.pageNo() - 1, cmd.pageSize());
        return JsonBean.ok(dataSourceService.findAllByPage(cmd, pageable));
    }

    @GetMapping("getAll")
    public JsonBean<List<DataSource>> getAll() {
        return JsonBean.ok(dataSourceService.findAll());
    }

    @GetMapping("getItemTitles")
    public JsonBean<List<ItemTitle>> getItemTitleList(@RequestParam("id") Long id,
                                                      @RequestParam(value = "title", required = false) String title) {
        return JsonBean.ok(dataSourceService.getItemTitleList(id, title));
    }

    @PostMapping("testRegexp")
    public JsonBean<List<MatchResult>> testRegexp(@RequestBody TestRegexp cmd) {
        return JsonBean.ok(dataSourceService.testRegexp(cmd));
    }

}
