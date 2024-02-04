package com.github.nekolr.peashooter.service;

import com.github.nekolr.peashooter.controller.request.group.SaveGroup;
import com.github.nekolr.peashooter.controller.request.group.GetGroupList;
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

    @Caching(evict = {@CacheEvict(key = "'all'"), @CacheEvict(key = "#p0.id", condition = "#p0.id != null")})
    Group save(Group group);

    Page<Group> findAllByPage(GetGroupList cmd, Pageable pageable);

    @Caching(evict = {@CacheEvict(key = "'all'"), @CacheEvict(key = "#p0.id", condition = "#p0.id != null")})
    void saveGroup(SaveGroup saveGroup);

    void refreshRss(Long groupId);

    String getRss(String filename);

    String getAllRss();
}
