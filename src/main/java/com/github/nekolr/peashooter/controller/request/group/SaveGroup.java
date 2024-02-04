package com.github.nekolr.peashooter.controller.request.group;

import com.github.nekolr.peashooter.rss.convert.Matcher;

import java.util.List;

public record SaveGroup(Long id,
                        String name,
                        String referenceId,
                        Long[] dataSourceIds,
                        String quality,
                        String language,
                        List<Matcher> matchers) {

}
