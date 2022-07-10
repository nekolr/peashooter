package com.github.nekolr.peashooter.entity.mapper;

import com.github.nekolr.peashooter.controller.req.group.AddGroup;
import com.github.nekolr.peashooter.entity.domain.Group;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GroupMapper {

    Group toDomain(AddGroup addGroup);
}
