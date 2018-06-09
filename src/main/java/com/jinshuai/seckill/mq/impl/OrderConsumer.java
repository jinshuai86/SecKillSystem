package com.jinshuai.seckill.mq;

import com.google.gson.Gson;
import com.jinshuai.seckill.dao.ISecKillDao;
import com.jinshuai.seckill.entity.Order;
import com.qianmi.ms.starter.rocketmq.annotation.RocketMQMessageListener;
import com.qianmi.ms.starter.rocketmq.core.RocketMQListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: JS
 * @date: 2018/6/9
 * @description:
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = "orderTopic", consumerGroup = "orderConsumerGroup-orderTopic")
public class OrderConsumer implements RocketMQListener<String>{

    @Autowired
    private ISecKillDao secKillDao;

    @Autowired
    private Gson gson;

    @Override
    public void onMessage(String message) {
        try {
            Order order = gson.fromJson(message,Order.class);
            int count = secKillDao.createOrder(order);
            if (count != 1) {
                log.error("创建订单[{}]失败",order);
            }
        } catch (Exception e) {
            log.error("消费者消费订单异常",e);
        }
    }
}
