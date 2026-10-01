package com.github.nekolr.peashooter.controller;

import com.github.nekolr.peashooter.newznab.model.NewznabSearchCriteria;
import com.github.nekolr.peashooter.service.INewznabService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.github.nekolr.peashooter.constant.Peashooter.*;

/**
 * Newznab/Torznab 索引器接口，供 Sonarr 作为 Torznab 索引器接入
 */
@Slf4j
@RestController
@RequestMapping("api/newznab")
@RequiredArgsConstructor
public class NewznabController {

    private final INewznabService newznabService;

    @GetMapping
    public void handle(@RequestParam(name = "t", required = false, defaultValue = "caps") String t,
                       @RequestParam(name = "cat", required = false) String cat,
                       @RequestParam(name = "q", required = false) String q,
                       @RequestParam(name = "tvdbid", required = false) String tvdbid,
                       @RequestParam(name = "imdbid", required = false) String imdbid,
                       // 绑定为 String：Sonarr 日播搜索会发 season=yyyy&ep=MM/dd，无法解析时忽略而不是 400
                       @RequestParam(name = "season", required = false) String season,
                       @RequestParam(name = "ep", required = false) String ep,
                       @RequestParam(name = "limit", required = false, defaultValue = "100") Integer limit,
                       @RequestParam(name = "offset", required = false, defaultValue = "0") Integer offset,
                       HttpServletResponse resp) throws IOException {
        String function = StringUtils.hasText(t) ? t.toLowerCase(Locale.ROOT) : "caps";
        String xml = switch (function) {
            case "caps" -> newznabService.getCaps();
            case "search", "tvsearch" -> newznabService.search(new NewznabSearchCriteria(function, parseCategories(cat),
                    q, tvdbid, imdbid, parseNumber(season), parseNumber(ep),
                    limit == null ? NEWZNAB_DEFAULT_LIMIT : limit,
                    offset == null ? 0 : offset));
            default -> newznabService.getError(202, "No such function");
        };

        OutputStream out = resp.getOutputStream();
        resp.setContentType(MediaType.APPLICATION_XML_VALUE);
        resp.setCharacterEncoding(CHARSET);
        StreamUtils.copy(xml, Charset.forName(CHARSET), out);
    }

    /**
     * 宽松解析数字参数，无法解析时返回 null（视为未提供）
     */
    private Integer parseNumber(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            log.debug("忽略无法解析的数字参数：{}", value);
            return null;
        }
    }

    private List<Integer> parseCategories(String cat) {
        List<Integer> categories = new ArrayList<>();
        if (!StringUtils.hasText(cat)) {
            return categories;
        }
        for (String value : cat.split(",")) {
            String trimmed = value.trim();
            if (StringUtils.hasText(trimmed)) {
                try {
                    categories.add(Integer.parseInt(trimmed));
                } catch (NumberFormatException e) {
                    log.debug("忽略非法分类：{}", trimmed);
                }
            }
        }
        return categories;
    }
}
