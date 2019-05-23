package com.jinshuai.seckill.mq.impl;

import com.alibaba.fastjson.JSON;
import com.jinshuai.seckill.mq.Consumer;
import com.jinshuai.seckill.order.dao.OrderDao;
import com.jinshuai.seckill.order.entity.Order;
import com.qianmi.ms.starter.rocketmq.annotation.RocketMQMessageListener;
import com.qianmi.ms.starter.rocketmq.core.RocketMQListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

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
    private OrderDao orderDao;

    @Autowired
    private JedisSentinelPool jedisSentinelPool;

    private AtomicInteger orderNums = new AtomicInteger(0);

    @Override
    public void onMessage(String message) {
        Order order = JSON.parseObject(message, Order.class);
        consume(order);
    }

    @Override
    public void consume(Order order) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            // 已经消费过此条消息
            if (jedis.sismember("orderUUID", order.getOrderUUID())) {
                log.info("消息[{}]已经被消费", order);
                return;
            }
            // 添加这条订单的UUID到Redis中
            jedis.sadd("orderUUID", order.getOrderUUID());
            orderDao.createOrder(order);
            log.info("订单出队成功，当前创建订单总量[{}]", orderNums.addAndGet(1));
        } catch (Exception e) {
            log.error("订单[{}]出队异常", order, e);
        }
    }

}