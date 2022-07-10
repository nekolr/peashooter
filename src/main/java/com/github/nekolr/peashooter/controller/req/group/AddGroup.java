package com.github.nekolr.peashooter.controller.req.group;

import com.github.nekolr.peashooter.rss.convert.Matcher;

import java.util.List;

public record AddGroup(String name,
                       String referenceId,
                       String datasourceIds,
                       String quality,
                       String language,
                       List<Matcher> matchers) {

}
