package com.github.nekolr.peashooter.controller.req.datasource;

public record AddDataSource(String name, String sourceUrl, Boolean useProxy, Integer refreshSeconds) {

}
