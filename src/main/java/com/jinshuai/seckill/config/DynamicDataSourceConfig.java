package com.jinshuai.seckill.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.jinshuai.seckill.constant.DataSourceConstant;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: JS
 * @date: 2018/9/19
 * @description:
 */
@Configuration
public class DynamicDataSourceConfig {

    /**
     * masterDataSource
     * */
    @Bean("masterDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.druid.master")
    public DruidDataSource getMasterDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    /**
     * slaveDataSource
     * */
    @Bean("slaveDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.druid.slave")
    public DruidDataSource getSlaveDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    /**
     * routingDataSource
     * */
    @Bean("dynamicDataSource")
    @Primary
    public DataSource getDataSource(
            @Autowired @Qualifier("masterDataSource")DataSource masterDataSource,
            @Autowired @Qualifier("slaveDataSource")DataSource slaveDataSource
    ) {
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put(DataSourceConstant.MASTER,masterDataSource);
        dataSourceMap.put(DataSourceConstant.SLAVE,slaveDataSource);
        RoutingDataSource routingDataSource = new RoutingDataSource();
        routingDataSource.setTargetDataSources(dataSourceMap);
        // 设置默认的数据源
        routingDataSource.setDefaultTargetDataSource(masterDataSource);
        return routingDataSource;
    }

    /**
     * 为SqlSessionFactoryBean配置动态数据源
     * */
    @Bean
    @ConfigurationProperties(prefix = "mybatis")
    @Primary
    public SqlSessionFactoryBean getSqlSessionFactoryBean(@Qualifier("dynamicDataSource") DataSource dynamicDataSource) {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dynamicDataSource);
        return sqlSessionFactoryBean;
    }

}