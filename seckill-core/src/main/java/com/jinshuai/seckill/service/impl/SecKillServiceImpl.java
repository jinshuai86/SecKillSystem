package com.jinshuai.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.jinshuai.seckill.account.dao.UserDao;
import com.jinshuai.seckill.account.entity.User;
import com.jinshuai.seckill.common.enums.StatusEnum;
import com.jinshuai.seckill.common.exception.SecKillException;
import com.jinshuai.seckill.mq.Producer;
import com.jinshuai.seckill.order.dao.OrderDao;
import com.jinshuai.seckill.order.entity.Order;
import com.jinshuai.seckill.product.dao.ProductDao;
import com.jinshuai.seckill.product.entity.Product;
import com.jinshuai.seckill.service.SecKillService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;

/**
 * @author: JS
 * @date: 2018/6/3
 * @description:
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class SecKillServiceImpl implements SecKillService {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private JedisSentinelPool jedisSentinelPool;

    private ThreadLocal<Jedis> jedisContainer = new ThreadLocal<>();

    @Autowired
    private Producer<Order> orderProducer;

    /**
     * 乐观锁：加缓存、加消息队列
     */
    @Override
    public StatusEnum updateStockByOptimisticLock(Map<String, Long> parameter) throws SecKillException {
        StatusEnum status = StatusEnum.SUCCESS;
        long productId = parameter.get("productId");
        long userId = parameter.get("userId");
        // 检查 -> 更新为非原子操作：可以在更新缓存库存以后检查缓存库存，如果是负数则更新失败，并将缓存库存恢复为0
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            jedisContainer.set(jedis);
            // 查看是否重复购买
            checkRepeat(userId, productId);
            // 限制请求次数
            limitRequestTimes(userId);
            /*
            * 检查库存
            *     - 库存充足:更新数据库库存
            *         - 更新成功:创建订单到队列
            *         - 更新失败:抛出系统太忙异常，前台可以提示用户稍后再试
            *     - 库存不足:抛出库存不足异常，返回对应JSON
            * */
            checkStock(productId, userId);
        } finally {
            jedisContainer.remove();
        }
        return status;
    }

    /**
     * 查看是否重复购买
     *
     */
    private void checkRepeat(long userId, long productId) throws SecKillException {
        Jedis jedis = jedisContainer.get();
        // 将用户Id和商品Id作为集合中唯一元素
        String itemKey = userId + ":" + productId;
        if (jedis.sismember("shopping:item", itemKey)) {
            throw new SecKillException(StatusEnum.REPEAT);
        }
    }

    /**
     * 限速：用户12秒内请求次数不能超过2次
     * TODO: 硬编码
     *
     */
    private void limitRequestTimes(long userId) throws SecKillException {
        Jedis jedis = jedisContainer.get();
        // 每个用户的请求标识
        String itemKey = "user:limit:" + userId;
        // 已经请求的次数
        String reqTimes = jedis.get(itemKey);
        // 第一次请求：设置初始值
        if (reqTimes == null) {
            jedis.set(itemKey, "1");
            jedis.expire(itemKey, 5);
        }
        // 限速
        else if (Long.valueOf(reqTimes) >= 10) {
            log.info("用户[{}]频繁请求", userId);
            throw new SecKillException(StatusEnum.FREQUENCY_REQUEST);
        }
        // 还没超过限制次数
        else {
            jedis.incr(itemKey);
        }
    }

    /**
     * 1. 检查库存
     * 2. 处理缓存穿透
     *
     */
    private void checkStock(long productId, long userId) throws SecKillException {
        Jedis jedis = jedisContainer.get();
        Product product;
        String cacheProductKey = "product:" + productId;
        String productJSON = jedis.get(cacheProductKey);
        // 命中无意义数据
        if ("penetration".equals(productJSON)) {
            throw new SecKillException(StatusEnum.INCOMPLETE_ARGUMENTS);
        }

        // 缓存未命中
        if (productJSON == null) {
            log.info("商品编号: [{}] 未在缓存命中", productId);
            product = productDao.getProductById(productId);
            // 数据库不存在此商品
            if (product == null) {
                log.info("商品编号: [{}] 未在数据库命中,已添加冗余数据到缓存", productId);
                // 通过缓存没意义的数据防止缓存穿透
                jedis.set(cacheProductKey, "penetration");
                throw new SecKillException(StatusEnum.INCOMPLETE_ARGUMENTS);
            } else {
                jedis.set(cacheProductKey, JSON.toJSONString(product));
                jedis.expire(cacheProductKey,2);
            }
        } else {
            log.info("商品编号: [{}] 命中缓存", productId);
            product = JSON.parseObject(productJSON, Product.class);
        }

        // 库存不足
        if (product.getStock() == 0) {
            throw new SecKillException(StatusEnum.LOW_STOCKS);
        }

        // 更新库存
        updateStock(product);

        // 创建订单
        User user = userDao.getUserById(userId);
        createOrder(product, user);
    }

    /**
     * 1.更新数据库库存
     * 2.删除缓存
     *
     */
    private void updateStock(Product product) throws SecKillException {
        Jedis jedis = jedisContainer.get();
        String cacheProductStockKey = "product:" + product.getId();
        // 更新数据库商品库存
        int count = productDao.updateStockByOptimisticLock(product);
        if (count != 1) {
            log.info("乐观锁更新商品编号: [{}] 失败", product.getId());
            throw new SecKillException(StatusEnum.SYSTEM_BUSY);
        } else {
            jedis.del(cacheProductStockKey);
        }
    }

    /**
     * 订单入队列，等待消费
     *
     */
    private void createOrder(Product product, User user) {
        Jedis jedis = jedisContainer.get();
        DateTime dateTime = new DateTime();
        Timestamp ts = new Timestamp(dateTime.getMillis());
        Order order = new Order(user, product, ts, UUID.randomUUID().toString());
        orderProducer.product(order);
        // 在Redis中缓存购买记录，防止重复购买
        String itemKey = user.getId() + ":" + product.getId();
        jedis.sadd("shopping:item", itemKey);
    }

    /**
     * 乐观锁：未加缓存
     *
     */
    public StatusEnum _updateStockByOptimisticLock(Map<String, Integer> parameter) {
        StatusEnum status = StatusEnum.SUCCESS;
        int productId = parameter.get("productId");
        Product product = productDao.getProductById(productId);
        int userId = parameter.get("userId");
        User user = userDao.getUserById(userId);
        try {
            // 更新/修改成功标志位
            int count = 0;
            // 检查库存
            if (product.getStock() > 0) {
                /* 更新库存 TODO:锁库存而不是更新库存：创建订单失败，此次操作需要回滚。 */
                count = productDao.updateStockByOptimisticLock(product);
                /* 更新库存失败 TODO:进行自旋重新尝试购买 */
                if (count != 1) {
                    status = StatusEnum.SYSTEM_EXCEPTION;
                } else {
                    // 创建订单
                    createOrder(product, user);
                }
            } else { // 库存不足
                status = StatusEnum.LOW_STOCKS;
                log.warn("库存不足 productId： [{}] productName：[{}]", productId, product.getProductName());
            }
        } catch (Exception e) {
            log.error("秒杀失败", e);
            status = StatusEnum.SYSTEM_EXCEPTION;
        }
        return status;
    }

    /**
     * 悲观锁
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public StatusEnum updateStockByPessimisticLock(Map<String, Long> parameter) {
        StatusEnum status = StatusEnum.SUCCESS;
        int count;
        try {
            long userId = parameter.get("userId");
            User user = userDao.getUserById(userId);
            long productId = parameter.get("productId");
            Product product = productDao.getAndLockProductById(productId);
            // 库存充足
            if (product.getStock() > 0) {
                // 更新库存
                count = productDao.updateStockByPessimisticLock(product);
                // 更新失败
                if (count != 1) {
                    status = StatusEnum.SYSTEM_EXCEPTION;
                } else {
                    // 创建订单
                    DateTime dateTime = new DateTime();
                    Timestamp ts = new Timestamp(dateTime.getMillis());
                    Order order = new Order(user, product, ts, UUID.randomUUID().toString());
                    orderDao.createOrder(order);
                }
            } else { // 库存不足
                status = StatusEnum.LOW_STOCKS;
                log.warn("库存不足 productId： [{}] productName：[{}]", productId, product.getProductName());
            }
        } catch (Exception e) {
            log.error("创建订单失败", e);
            status = StatusEnum.SYSTEM_EXCEPTION;
        }
        return status;
    }

}