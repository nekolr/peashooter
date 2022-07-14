package com.github.nekolr.peashooter.controller;

import com.github.nekolr.peashooter.controller.req.datasource.GetDataSourceList;
import com.github.nekolr.peashooter.entity.JsonBean;
import com.github.nekolr.peashooter.entity.domain.DataSource;
import com.github.nekolr.peashooter.service.IDataSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/datasource")
@RequiredArgsConstructor
public class DataSourceController {

    private final IDataSourceService dataSourceService;

    @PostMapping("save")
    public JsonBean<Void> save(@RequestBody DataSource dataSource) {
        dataSourceService.save(dataSource);
        return JsonBean.ok();
    }

    @PostMapping("getList")
    public JsonBean<Page<DataSource>> getDataSourceList(@RequestBody GetDataSourceList cmd) {
        Pageable pageable = PageRequest.of(cmd.pageNo() - 1, cmd.pageSize());
        return JsonBean.ok(dataSourceService.findAllByPage(cmd, pageable));
    }
}
