package com.jinshuai.seckill.aspect;

import com.jinshuai.seckill.annotation.TargetDataSource;
import com.jinshuai.seckill.config.RoutingDataSourceContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * @author: JS
 * @date: 2018/9/19
 * @description:
 */
@Aspect
@Component
@Slf4j
public class DataSourceAspect {

    @Before("@annotation(com.jinshuai.seckill.annotation.TargetDataSource)")
    public void doBefore(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        TargetDataSource targetDataSourceAnnotation = methodSignature.getMethod().getAnnotation(TargetDataSource.class);
        if (targetDataSourceAnnotation != null) {
            String dataSourceKey = targetDataSourceAnnotation.value();
            RoutingDataSourceContext routingDataSourceContext = RoutingDataSourceContext.getInstance();
            routingDataSourceContext.setThreadLocalDataSourceKey(dataSourceKey);
//            log.info("用的数据源：{}",dataSourceKey);
        }
    }

    @After("@annotation(com.jinshuai.seckill.annotation.TargetDataSource)")
    public void doAfter() {
        RoutingDataSourceContext routingDataSourceContext = RoutingDataSourceContext.getInstance();
        routingDataSourceContext.removeThreadLocalDataSourceKey();
//        log.info("cleaned the key of dataSource in threadLocal");
    }
}