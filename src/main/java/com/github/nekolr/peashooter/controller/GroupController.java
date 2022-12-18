package com.github.nekolr.peashooter.controller;

import com.github.nekolr.peashooter.controller.req.group.GetGroupList;
import com.github.nekolr.peashooter.controller.req.group.SaveGroup;
import com.github.nekolr.peashooter.entity.JsonBean;
import com.github.nekolr.peashooter.entity.domain.Group;
import com.github.nekolr.peashooter.service.IGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import static com.github.nekolr.peashooter.constant.Peashooter.CHARSET;

@RestController
@RequestMapping("api/group")
@RequiredArgsConstructor
public class GroupController {
    private final IGroupService groupService;

    @PostMapping("save")
    public JsonBean<Void> save(@RequestBody SaveGroup saveGroup) {
        groupService.saveGroup(saveGroup);
        return JsonBean.ok();
    }

    @GetMapping("{filename}")
    public void getGroupRss(@PathVariable String filename, HttpServletResponse resp) {
        String rss = groupService.getRss(filename);
        try {
            OutputStream out = resp.getOutputStream();
            resp.setContentType(MediaType.APPLICATION_RSS_XML_VALUE);
            resp.setCharacterEncoding(CHARSET);
            StreamUtils.copy(rss, Charset.forName(CHARSET), out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("allRss.xml")
    public void getAllGroupRss(HttpServletResponse resp) {
        String rss = groupService.getAllRss();
        try {
            OutputStream out = resp.getOutputStream();
            resp.setContentType(MediaType.APPLICATION_RSS_XML_VALUE);
            resp.setCharacterEncoding(CHARSET);
            StreamUtils.copy(rss, Charset.forName(CHARSET), out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("getList")
    public JsonBean<Page<Group>> getGroupList(@RequestBody GetGroupList cmd) {
        Pageable pageable = PageRequest.of(cmd.pageNo() - 1, cmd.pageSize());
        return JsonBean.ok(groupService.findAllByPage(cmd, pageable));
    }

    @GetMapping("getGroup")
    public JsonBean<Group> getGroup(@RequestParam("id") Long id) {
        return JsonBean.ok(groupService.getById(id));
    }

    @PostMapping("delete")
    public JsonBean<Void> delete(@RequestParam("id") Long id) {
        groupService.removeById(id);
        return JsonBean.ok();
    }

    @PostMapping("refreshRss")
    public JsonBean<Void> refreshRss(@RequestParam("id") Long id) {
        groupService.refreshRss(id);
        return JsonBean.ok();
    }
}
