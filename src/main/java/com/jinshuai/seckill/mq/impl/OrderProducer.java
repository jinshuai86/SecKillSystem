package com.jinshuai.seckill.mq.impl;

import com.jinshuai.seckill.entity.Order;
import com.jinshuai.seckill.mq.Producer;
import com.qianmi.ms.starter.rocketmq.core.RocketMQTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author: JS
 * @date: 2018/6/9
 * @description:
 */
@Slf4j
@Service
public class OrderProducer implements Producer<Order> {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public int product(Order order) {
        int count = 1;
        try {
            rocketMQTemplate.convertAndSend("orderTopic", order);
//            log.info("订单入队成功[{}]",order);
        } catch (Exception e) {
            log.error("订单入队异常",e);
            count = 0;
        }
        return count;
    }

}
