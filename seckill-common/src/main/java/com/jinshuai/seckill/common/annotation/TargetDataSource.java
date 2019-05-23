package com.jinshuai.seckill.common.annotation;

import com.jinshuai.seckill.common.constant.DataSourceConstant;

import java.lang.annotation.*;

/**
 * @author: JS
 * @date: 2018/9/19
 * @description: 标记目标数据源
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TargetDataSource {

    /**
     * 目标数据源
     */
    String value() default DataSourceConstant.MASTER;

}