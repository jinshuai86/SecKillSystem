package com.jinshuai.seckill.exception.handler;

import com.jinshuai.seckill.enums.StatusEnum;
import com.jinshuai.seckill.exception.SecKillException;
import com.jinshuai.seckill.web.response.SecKillResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessagingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author: JS
 * @date: 2018/6/11
 * @description: 对SecKillException进行处理
 */
@ControllerAdvice
@Slf4j
public class SecKillExceptionHandler {

    /**
     * 将异常信息提取构造响应信息
     * */
    @ExceptionHandler(value = SecKillException.class)
    @ResponseBody
    public SecKillResponse SecKillExceptionHandle(SecKillException e) {
        String status = e.getStatusEnum().getStatus();
        String statusCode = e.getStatusEnum().getStatusCode();
        SecKillResponse secKillResponse = new SecKillResponse(status,statusCode);
        log.warn(status);
        return secKillResponse;
    }

    /**
     * 订单入队异常
     * */
    @ExceptionHandler(value = MessagingException.class)
    @ResponseBody
    public SecKillResponse SecKillExceptionHandle(MessagingException e) {
        StatusEnum statusEnum = StatusEnum.ORDER_ERROR;
        String status = statusEnum.getStatus();
        String statusCode = statusEnum.getStatusCode();
        SecKillResponse secKillResponse = new SecKillResponse(status,statusCode);
        log.error(status,e);
        return secKillResponse;
    }

}