package com.jinshuai.seckill.mq;

import com.jinshuai.seckill.entity.Order;
import com.qianmi.ms.starter.rocketmq.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: JS
 * @date: 2018/6/9
 * @description:
 */
@Service
public class OrderProducer implements Producer{

    @Autowired
    RocketMQTemplate rocketMQTemplate;

    @Override
    public void product(Order order) {
        rocketMQTemplate.convertAndSend("orderTopic", order);
    }

}
