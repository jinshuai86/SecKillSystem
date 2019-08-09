package com.jinshuai.seckill.mq.impl;

import com.alibaba.fastjson.JSON;
import com.jinshuai.seckill.common.util.RedisUtil;
import com.jinshuai.seckill.mq.Consumer;
import com.jinshuai.seckill.order.dao.OrderDao;
import com.jinshuai.seckill.order.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: JS
 * @date: 2018/6/9
 * @description:
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = "orderTopic", consumerGroup = "orderConsumerGroup-orderTopic", messageModel = MessageModel.CLUSTERING)
public class OrderConsumer implements RocketMQListener<String>, Consumer<Order> {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private JedisSentinelPool jedisSentinelPool;

    @Value("${redis.lock.expire}")
    private int lockExpire;

    private AtomicInteger orderNums = new AtomicInteger(0);

    private static final String LOCK_KEY = "ORDER_MESSAGE_LOCK";

    @Override
    public void onMessage(String message) {
        Order order = JSON.parseObject(message, Order.class);
        consume(order);
    }

    @Override
    public void consume(Order order) {
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            // 获取分布式锁
            String requestId = UUID.randomUUID().toString();
            RedisUtil.tryGetDistributedLock(jedis, LOCK_KEY, requestId, lockExpire);

            // 已经消费过此条消息
            String orderIdStr = String.valueOf(order.getOrderId());
            if (jedis.sismember("order:message:id", orderIdStr)) {
                log.warn("消息[{}]已经被消费", order);
                return;
            }
            // 添加这条订单的唯一业务标识到Redis中
            jedis.sadd("order:message:id", orderIdStr);

            // 释放【当前客户端】持有的锁
            RedisUtil.releaseDistributedLock(jedis, LOCK_KEY, requestId);

            orderDao.createOrder(order);
            log.debug("订单出队成功，当前创建订单总量 [{}]", orderNums.addAndGet(1));
        } catch (Exception e) {
            log.error("订单[{}]出队异常", order, e);
        }
    }

}