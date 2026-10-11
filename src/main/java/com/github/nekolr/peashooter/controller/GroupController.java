package com.github.nekolr.peashooter.controller;

import com.github.nekolr.peashooter.controller.cmd.group.GetGroupListCmd;
import com.github.nekolr.peashooter.controller.cmd.group.SaveGroupCmd;
import com.github.nekolr.peashooter.controller.vo.group.GroupVo;
import com.github.nekolr.peashooter.dto.JsonBean;
import com.github.nekolr.peashooter.service.IGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import static com.github.nekolr.peashooter.constant.Peashooter.CHARSET;

@RestController
@RequestMapping("api/group")
@RequiredArgsConstructor
public class GroupController {

    private final IGroupService groupService;

    /**
     * 保存分组
     */
    @PostMapping("save")
    public JsonBean<Void> save(@RequestBody SaveGroupCmd saveGroupCmd) {
        groupService.saveGroup(saveGroupCmd);
        return JsonBean.ok();
    }

    /**
     * 合法的 rss 文件名（纯字母数字 + .xml 后缀），防止路径穿越
     */
    private static final Pattern VALID_FILENAME_PATTERN = Pattern.compile("^[0-9A-Za-z]+\\.xml$");

    /**
     * 获取分组的 rss 文件
     */
    @GetMapping("{filename}")
    public void getGroupRss(@PathVariable String filename, HttpServletResponse resp) throws IOException {
        if (!VALID_FILENAME_PATTERN.matcher(filename).matches()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String rss = groupService.getRss(filename);
        if (!StringUtils.hasText(rss)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            OutputStream out = resp.getOutputStream();
            resp.setContentType(MediaType.APPLICATION_RSS_XML_VALUE);
            resp.setCharacterEncoding(CHARSET);
            StreamUtils.copy(rss, Charset.forName(CHARSET), out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取全部分组的 rss 文件
     */
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

    /**
     * 获取分组列表，带分页
     */
    @PostMapping("getList")
    public JsonBean<Page<GroupVo>> getGroupList(@RequestBody GetGroupListCmd cmd) {
        Pageable pageable = clampPageable(cmd.pageNo(), cmd.pageSize());
        return JsonBean.ok(groupService.findAllByPage(cmd, pageable));
    }

    /**
     * 获取分组
     */
    @GetMapping("getGroup")
    public JsonBean<GroupVo> getGroup(@RequestParam("id") Long id) {
        return JsonBean.ok(groupService.getById(id));
    }

    /**
     * 删除分组
     */
    @PostMapping("delete")
    public JsonBean<Void> delete(@RequestParam("id") Long id) {
        groupService.removeById(id);
        return JsonBean.ok();
    }

    /**
     * 刷新分组的 rss 文件
     */
    @PostMapping("refreshRss")
    public JsonBean<Void> refreshRss(@RequestParam("id") Long id) {
        groupService.refreshRss(id);
        return JsonBean.ok();
    }

    /**
     * 分页参数钳制：null 默认 (1, 10)，pageSize 上限 100
     */
    private static Pageable clampPageable(Integer pageNo, Integer pageSize) {
        int page = (pageNo == null || pageNo < 1) ? 1 : pageNo;
        int size = (pageSize == null || pageSize < 1) ? 10 : Math.min(pageSize, 100);
        return PageRequest.of(page - 1, size);
    }
}
