package com.github.nekolr.peashooter.service;

import com.github.nekolr.peashooter.controller.request.group.SaveGroup;
import com.github.nekolr.peashooter.controller.request.group.GetGroupList;
import com.github.nekolr.peashooter.entity.domain.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IGroupService {

    List<Group> findAll();

    void removeById(Long id);

    Group getById(Long id);

    Group save(Group group);

    Page<Group> findAllByPage(GetGroupList cmd, Pageable pageable);

    void saveGroup(SaveGroup saveGroup);

    void refreshRss(Long groupId);

    String getRss(String filename);

    String getAllRss();
}
