package com.jinshuai.seckill.common.config;

import com.jinshuai.seckill.common.constant.DataSourceConstant;

/**
 * @author: JS
 * @date: 2018/9/19
 * @description: 保存注解上的目标数据源到threadLocalKey
 */
public class RoutingDataSourceContext {

    private final ThreadLocal<String> threadLocalDataSourceKey = new ThreadLocal<>();

    private static volatile RoutingDataSourceContext instance;

    public static RoutingDataSourceContext getInstance() {
        if (instance == null) {
            synchronized (RoutingDataSourceContext.class) {
                if (instance == null) {
                    instance = new RoutingDataSourceContext();
                }
            }
        }
        return instance;
    }

    public void setThreadLocalDataSourceKey(String key) {
        key = (key == null ? DataSourceConstant.MASTER : key);
        this.threadLocalDataSourceKey.set(key);
    }

    public String getThreadLocalDataSourceKey() {
        return this.threadLocalDataSourceKey.get();
    }

    public void removeThreadLocalDataSourceKey() {
        this.threadLocalDataSourceKey.remove();
    }

}