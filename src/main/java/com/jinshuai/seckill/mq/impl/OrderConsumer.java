package com.jinshuai.seckill.mq.impl;

import com.alibaba.fastjson.JSON;
import com.jinshuai.seckill.dao.ISecKillDao;
import com.jinshuai.seckill.entity.Order;
import com.jinshuai.seckill.mq.Consumer;
import com.qianmi.ms.starter.rocketmq.annotation.RocketMQMessageListener;
import com.qianmi.ms.starter.rocketmq.core.RocketMQListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: JS
 * @date: 2018/6/9
 * @description:
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = "orderTopic", consumerGroup = "orderConsumerGroup-orderTopic")
public class OrderConsumer implements RocketMQListener<String>, Consumer<Order> {

    @Autowired
    private ISecKillDao secKillDao;

    @Autowired
    private JedisPool jedisPool;

    @Override
    public void onMessage(String message) {
        Order order = JSON.parseObject(message,Order.class);
        // 已经消费过此条消息
        if (jedisPool.getResource().sismember("orderUUID",order.getOrderUUID())) {
            log.error("消息[{}]被重复消费",order);
        }
        jedisPool.getResource().sadd("orderUUID",order.getOrderUUID());
        consume(order);
    }

    private AtomicInteger orderNums = new AtomicInteger(0);

    @Override
    public void consume(Order order) {
        try {
            int count = secKillDao.createOrder(order);
            if (count != 1) {
                log.error("订单[{}]出队进入数据库失败",order);
            } else {
                log.info("订单[{}]出队成功，当前创建订单总量[{}]",order.getId(), orderNums.addAndGet(1));
            }
        } catch (Exception e) {
            log.error("订单[{}]出队异常",order.getId(),e);
        }
    }
}