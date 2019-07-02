package com.jinshuai.seckill.mq.impl;

import com.jinshuai.seckill.mq.Producer;
import com.jinshuai.seckill.order.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: JS
 * @date: 2018/6/9
 * @description:
 */
@Slf4j
@Service
public class OrderProducer implements Producer<Order> {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public void product(Order order) {
        rocketMQTemplate.setMessageQueueSelector((list, message, o) -> list.get(order.getId() % list.size()));
        rocketMQTemplate.convertAndSend("orderTopic", order);
    }

}