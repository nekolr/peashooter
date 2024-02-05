package com.github.nekolr.peashooter.controller.request.datasource;

import com.github.nekolr.peashooter.rss.convertor.Matcher;

public record TestRegexp(Matcher matcher, String[] dataSourceIds, String series, String quality, String language) {

}
