package com.github.nekolr.peashooter.controller;

import com.github.nekolr.peashooter.controller.req.datasource.AddDataSource;
import com.github.nekolr.peashooter.entity.JsonBean;
import com.github.nekolr.peashooter.service.IDataSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/datasource")
@RequiredArgsConstructor
public class DataSourceController {

    private final IDataSourceService dataSourceService;

    @PostMapping("add")
    public JsonBean<Void> add(@RequestBody AddDataSource addDataSource) {
        dataSourceService.add(addDataSource);
        return JsonBean.ok();
    }
}
