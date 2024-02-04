package com.github.nekolr.peashooter.controller.request.datasource;

import com.github.nekolr.peashooter.rss.convert.Matcher;

public record TestRegexp(Matcher matcher, String[] dataSourceIds, String series, String quality, String language) {

}
