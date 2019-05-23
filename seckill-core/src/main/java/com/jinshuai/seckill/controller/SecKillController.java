package com.jinshuai.seckill.controller;

import com.jinshuai.seckill.common.enums.StatusEnum;
import com.jinshuai.seckill.common.exception.SecKillException;
import com.jinshuai.seckill.service.SecKillService;
import com.jinshuai.seckill.common.web.SecKillRequest;
import com.jinshuai.seckill.common.web.SecKillResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: JS
 * @date: 2018/6/3
 * @description: 秒杀请求接口
 */
@RestController
@Slf4j
public class SecKillController {

    @Autowired
    private SecKillService secKillService;

    /**
     * 乐观锁
     */
    @PostMapping(value = "/optimistic")
    SecKillResponse updateStockByOptimisticLock(@RequestBody SecKillRequest secKillRequest) throws SecKillException {
        // 初始化响应信息
        StatusEnum statusEnum = StatusEnum.INCOMPLETE_ARGUMENTS;
        SecKillResponse secKillResponse = new SecKillResponse(statusEnum.getStatus(), statusEnum.getStatusCode());
        // 检查请求参数
        if (secKillRequest == null || secKillRequest.getProductId() == null || secKillRequest.getUserId() == null) {
            throw new SecKillException(statusEnum);
        } else {
            // 封装请求参数
            Map<String, Integer> requestParameter = new HashMap<>();
            requestParameter.put("productId", secKillRequest.getProductId());
            requestParameter.put("userId", secKillRequest.getUserId());
            // 更新库存
            statusEnum = secKillService.updateStockByOptimisticLock(requestParameter);
            // 封装响应参数
            secKillResponse.setStatus(statusEnum.getStatus())
                    .setStatusCode(statusEnum.getStatusCode());
        }
        return secKillResponse;
    }

    /**
     * 悲观锁
     */
    @PostMapping("/pessimistic")
    SecKillResponse updateStockByPessimisticLock(@RequestBody SecKillRequest secKillRequest) {

        StatusEnum statusEnum = StatusEnum.INCOMPLETE_ARGUMENTS;
        SecKillResponse secKillResponse = new SecKillResponse(statusEnum.getStatus(), statusEnum.getStatusCode());

        if (secKillRequest == null || secKillRequest.getProductId() == null || secKillRequest.getUserId() == null) {
            log.error("请求参数不完整[{}]", secKillRequest.toString());
            return secKillResponse;
        } else {
            Map<String, Integer> parameterMap = new HashMap<>();
            parameterMap.put("userId", secKillRequest.getUserId());
            parameterMap.put("productId", secKillRequest.getProductId());

            statusEnum = secKillService.updateStockByPessimisticLock(parameterMap);
            secKillResponse.setStatus(statusEnum.getStatus())
                    .setStatusCode(statusEnum.getStatusCode());
        }
        return secKillResponse;
    }

}