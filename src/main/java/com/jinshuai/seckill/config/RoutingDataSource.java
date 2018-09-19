package com.jinshuai.seckill.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @author: JS
 * @date: 2018/9/19
 * @description: 根据上下文中的key路由到指定的数据源
 */
public class RoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        return RoutingDataSourceContext.getInstance().getThreadLocalDataSourceKey();
    }
}
