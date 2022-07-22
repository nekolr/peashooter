package com.github.nekolr.peashooter.service;

import com.github.nekolr.peashooter.controller.req.group.SaveGroup;
import com.github.nekolr.peashooter.controller.req.group.GetGroupList;
import com.github.nekolr.peashooter.entity.domain.Group;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@CacheConfig(cacheNames = "group")
public interface IGroupService {

    @Cacheable(key = "'all'")
    List<Group> findAll();

    @Caching(evict = {@CacheEvict(key = "'all'"), @CacheEvict(key = "#p0")})
    void removeById(Long id);

    @Cacheable(key = "#p0")
    Group getById(Long id);

    @CacheEvict(key = "'all'")
    Group save(Group group);

    Page<Group> findAllByPage(GetGroupList cmd, Pageable pageable);

    @CacheEvict(key = "'all'")
    void saveGroup(SaveGroup saveGroup);

    void refreshRss(Long groupId);

    String getRss(String filename);

    String getAllRss();
}
