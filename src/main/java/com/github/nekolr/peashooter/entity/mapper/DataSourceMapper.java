package com.github.nekolr.peashooter.entity.mapper;

import com.github.nekolr.peashooter.controller.req.datasource.AddDataSource;
import com.github.nekolr.peashooter.entity.domain.DataSource;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DataSourceMapper {

    DataSource toDomain(AddDataSource addDataSource);

}
