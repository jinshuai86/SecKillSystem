package com.jinshuai.seckill.controller;

import com.jinshuai.seckill.common.Message;
import com.jinshuai.seckill.enums.StatusEnum;
import com.jinshuai.seckill.service.ISecKillService;
import com.jinshuai.seckill.web.request.SecKillRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: JS
 * @date: 2018/6/3
 * @description:
 * 秒杀请求接口
 */
@RestController
public class SecKillController {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ISecKillService secKillService;

    /**
     * 乐观锁
     * */
    @PostMapping(value = "/optimistic")
    Message updateStockByOptimisticLock(@RequestBody SecKillRequest secKillRequest) {
        // 初始化响应信息
        StatusEnum statusEnum = StatusEnum.INCOMPLETE_ARGUMENTS;
        Message responseMessage = new Message(statusEnum.getStatus(),statusEnum.getStatusCode());
        // 检查请求参数
        if (secKillRequest == null || secKillRequest.getProductId() == null || secKillRequest.getUserId() == null) {
            LOGGER.error("请求参数不完整[{}]",secKillRequest.toString());
            return responseMessage;
        } else {
            // 封装请求参数
            Map<String,Integer> requestParameter = new HashMap<>();
            requestParameter.put("productId",secKillRequest.getProductId());
            requestParameter.put("userId",secKillRequest.getUserId());
            // 更新库存
            statusEnum = secKillService.updateStockByOptimisticLock(requestParameter);
            // 封装响应参数
            responseMessage.setStatus(statusEnum.getStatus())
                    .setStatusCode(statusEnum.getStatusCode());
        }
        return responseMessage;
    }

    /**
     * 悲观锁
     * */
    @PostMapping("/pessimistic")
    Message updateStockByPessimisticLock(@RequestBody SecKillRequest secKillRequest) {

        StatusEnum statusEnum = StatusEnum.INCOMPLETE_ARGUMENTS;
        Message message = new Message(statusEnum.getStatus(),statusEnum.getStatusCode());

        if (secKillRequest == null || secKillRequest.getProductId() == null || secKillRequest.getUserId() == null) {
            LOGGER.error("请求参数不完整[{}]",secKillRequest.toString());
            return message;
        } else {
            Map<String,Integer> parameterMap = new HashMap<>();
            parameterMap.put("userId",secKillRequest.getUserId());
            parameterMap.put("productId",secKillRequest.getProductId());

            statusEnum = secKillService.updateStockByPessimisticLock(parameterMap);
            message.setStatus(statusEnum.getStatus())
                    .setStatusCode(statusEnum.getStatusCode());
        }
        return message;
    }

}