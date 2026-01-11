package com.github.nekolr.peashooter.service;

import com.github.nekolr.peashooter.controller.cmd.group.SaveGroupCmd;
import com.github.nekolr.peashooter.controller.cmd.group.GetGroupListCmd;
import com.github.nekolr.peashooter.controller.vo.group.GroupVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IGroupService {

    /**
     * 删除分组
     */
    void removeById(Long id);

    /**
     * 获取分组
     */
    GroupVo getById(Long id);

    /**
     * 获取分组列表，带分页
     */
    Page<GroupVo> findAllByPage(GetGroupListCmd cmd, Pageable pageable);

    /**
     * 保存分组
     */
    void saveGroup(SaveGroupCmd saveGroupCmd);

    /**
     * 刷新分组的 rss 文件
     */
    void refreshRss(Long id);

    /**
     * 获取分组的 rss 文件
     */
    String getRss(String filename);

    /**
     * 获取全部分组的 rss 文件
     */
    String getAllRss();


}
