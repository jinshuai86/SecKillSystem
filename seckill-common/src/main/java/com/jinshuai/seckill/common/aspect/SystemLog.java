package com.jinshuai.seckill.common.aspect;

import com.jinshuai.seckill.common.annotation.APIOperation;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * 定义切面类，将自定义注解作为切点，获取操作信息。
 *
 * @author JS
 * @date 2019/6/19
 */
@Aspect
@Component
@Slf4j
public class SystemLog {

    @Pointcut("@annotation(com.jinshuai.seckill.common.annotation.APIOperation)")
    public void MethodAspect() {
    }

    @After("MethodAspect()")
    public void doAfter(JoinPoint joinPoint) {
        MethodSignature method = (MethodSignature) joinPoint.getSignature();
        APIOperation apiOperation = method.getMethod().getAnnotation(APIOperation.class);
        if (apiOperation != null) {
            log.info("请求方法:"
                    + (joinPoint.getTarget().getClass().getName() + "."
                    + joinPoint.getSignature().getName() + "()"));
            log.info("方法描述:"
                    + apiOperation.description());
        }
    }

}