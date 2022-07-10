package com.github.nekolr.peashooter.controller;

import com.github.nekolr.peashooter.controller.req.group.AddGroup;
import com.github.nekolr.peashooter.entity.JsonBean;
import com.github.nekolr.peashooter.service.IGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import static com.github.nekolr.peashooter.constant.Peashooter.CHARSET;

@RestController
@RequestMapping("group")
@RequiredArgsConstructor
public class GroupController {
    private final IGroupService groupService;

    @PostMapping("add")
    public JsonBean<Void> add(@RequestBody AddGroup addGroup) {
        groupService.add(addGroup);
        return JsonBean.ok();
    }

    @GetMapping("{filename}")
    public void getGroupRss(@PathVariable String filename, HttpServletResponse resp) {
        String rss = groupService.getRss(filename);
        try {
            OutputStream out = resp.getOutputStream();
            StreamUtils.copy(rss, Charset.forName(CHARSET), out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
