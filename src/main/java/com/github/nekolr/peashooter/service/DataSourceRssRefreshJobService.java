package com.github.nekolr.peashooter.service;

public interface DataSourceRssRefreshJobService {

    void doExecute(Long datasourceId);
}
