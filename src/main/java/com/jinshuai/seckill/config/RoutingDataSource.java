package com.jinshuai.seckill.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @author: JS
 * @date: 2018/9/19
 * @description: 根据上下文中的key路由到指定的数据源
 * 将ThreadLocal中存放的值作为这个 “路由器类”，路由指定到数据源所需要的key
 */
public class RoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        return RoutingDataSourceContext.getInstance().getThreadLocalDataSourceKey();
    }
}
