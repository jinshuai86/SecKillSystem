package com.jinshuai.seckill.mq.impl;

import com.jinshuai.seckill.mq.Producer;
import com.jinshuai.seckill.order.entity.Order;
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
    public void product(Order order) {
        rocketMQTemplate.convertAndSend("orderTopic", order);
        // log.info("订单入队成功[{}]",order);
    }

}