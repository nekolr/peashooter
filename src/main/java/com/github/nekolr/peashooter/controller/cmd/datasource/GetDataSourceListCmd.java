package com.github.nekolr.peashooter.controller.cmd.datasource;

/**
 * 数据源名称
 *
 * @param dataSourceName
 * @param pageNo
 * @param pageSize
 */
public record GetDataSourceListCmd(String dataSourceName, Integer pageNo, Integer pageSize) {
}
