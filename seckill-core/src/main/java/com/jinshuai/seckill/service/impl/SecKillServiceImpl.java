package com.jinshuai.seckill.service.impl;

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
    public StatusEnum updateStockByOptimisticLock(Map<String, Integer> parameter) throws SecKillException {
        StatusEnum status = StatusEnum.SUCCESS;
        int productId = parameter.get("productId");
        int userId = parameter.get("userId");
        User user = userDao.getUserById(userId);
        // 检查 -> 更新为非原子操作：可以在更新缓存库存以后检查缓存库存，如果是负数则更新失败，并将缓存库存恢复为0
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            jedisContainer.set(jedis);
            Product product = productDao.getProductById(productId);
            // 查看是否重复购买
            checkRepeat(user, product);
            // 限制请求次数
            limitRequestTimes(user, product);
            // 查看缓存库存
            checkStock(product);
            // 更新库存
            updateStock(product);
            // 创建订单
            createOrder(product, user);
        } finally {
            jedisContainer.remove();
        }
        return status;
    }

    /**
     * 查看是否重复购买
     */
    private void checkRepeat(User user, Product product) throws SecKillException {
        Jedis jedis = jedisContainer.get();
        // 将用户Id和商品Id作为集合中唯一元素
        String itemKey = user.getId() + ":" + product.getId();
        if (jedis.sismember("item", itemKey)) {
            throw new SecKillException(StatusEnum.REPEAT);
        }
    }

    /**
     * 限速：用户12秒内请求次数不能超过2次
     * TODO: 硬编码
     */
    private void limitRequestTimes(User user, Product product) throws SecKillException {
        Jedis jedis = jedisContainer.get();
        // 每个用户的请求标识
        String itemKey = "user:limit:" + user.getId();
        // 已经请求的次数
        String reqTimes = jedis.get(itemKey);
        // 第一次请求：设置初始值
        if (reqTimes == null) {
            jedis.set(itemKey, "1");
            jedis.expire(itemKey, 5);
        }
        // 限速
        else if (Integer.valueOf(reqTimes) >= 10) {
            log.warn("用户[{}]频繁请求商品[{}]", user.getId(), product.getId());
            throw new SecKillException(StatusEnum.FREQUENCY_REQUEST);
        }
        // 还没超过限制次数
        else {
            jedis.incr(itemKey);
        }
    }

    /**
     * 1. 处理缓存穿透 2. 检查库存
     */
    private void checkStock(Product product) throws SecKillException {
        Jedis jedis = jedisContainer.get();
        int productId = product.getId();
        String cacheProductKey = "product:" + productId + ":stock";
        String stockStr = jedis.get(cacheProductKey);
        // 缓存未命中
        if (stockStr == null) {
            log.warn("商品编号: [{}] 未在缓存命中", productId);
            // 在存储层去查找
            product = productDao.getProductById(productId);
            // 存储层不存在此商品
            if (product == null) {
                log.warn("商品编号: [{}] 未在存储层命中,已添加冗余数据到缓存", productId);
                // 通过缓存没意义的数据防止缓存穿透
                jedis.set(cacheProductKey, "penetration");
                throw new SecKillException(StatusEnum.INCOMPLETE_ARGUMENTS);
            }
            // 存储层存在此商品，添加到缓存：先判断再修改会导致并发修改不安全，通过加锁避免
            else {
                synchronized (this) {
                    // 如果没有在缓存中设置此商品，再设置
                    if (jedis.get(cacheProductKey) == null) {
                        jedis.set(cacheProductKey, String.valueOf(product.getStock()));
                    }
                    // 检查此商品在缓存里的库存
                    checkCacheStock(productId);
                }
            }
        }
        // 缓存命中
        else {
            // 命中无意义数据
            if ("penetration".equals(stockStr)) {
                throw new SecKillException(StatusEnum.INCOMPLETE_ARGUMENTS);
            } else {
                // 检查此商品在缓存里的库存
                checkCacheStock(productId);
            }
        }
    }

    /**
     * 查看缓存中的此商品的数量
     */
    private void checkCacheStock(int productId) throws SecKillException {
        Jedis jedis = jedisContainer.get();
        String cacheProductKey = "product:" + productId + ":stock";
        String stockStr = jedis.get(cacheProductKey);
        int cacheStock = Integer.valueOf(stockStr);
        if (cacheStock == 0) {
            throw new SecKillException(StatusEnum.LOW_STOCKS);
        }
    }

    /**
     * 更新缓存库存和数据库库存
     */
    private void updateStock(Product product) throws SecKillException {
        Jedis jedis = jedisContainer.get();
        int productId = product.getId();
        String cacheProductStockKey = "product:" + productId + ":stock";
        // 更新缓存库存
        long currentCacheStock = jedis.decr(cacheProductStockKey);
        // 防止并发修改导致超卖
        if (currentCacheStock < 0) {
            jedis.set(cacheProductStockKey, String.valueOf(0));
            throw new SecKillException(StatusEnum.LOW_STOCKS);
        } else {
            // 更新数据库商品库存
            int count = productDao.updateStockByOptimisticLock(product);
            if (count != 1) {
                // 更新数据库商品库存失败，回滚之前修改的缓存库存
                jedis.incr(cacheProductStockKey);
                throw new SecKillException(StatusEnum.LOW_STOCKS);
            }
        }
    }

    /**
     * 订单入队列，等待消费
     */
    private void createOrder(Product product, User user) {
        Jedis jedis = jedisContainer.get();
        DateTime dateTime = new DateTime();
        Timestamp ts = new Timestamp(dateTime.getMillis());
        Order order = new Order(user, product, ts, UUID.randomUUID().toString());
        // 放到消息队列 TODO 可以提示用户正在排队中... ...
        orderProducer.product(order);
        // 在Redis中缓存购买记录，防止重复购买
        String itemKey = user.getId() + ":" + product.getId();
        jedis.sadd("item", itemKey);
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
    public StatusEnum updateStockByPessimisticLock(Map<String, Integer> parameter) {
        StatusEnum status = StatusEnum.SUCCESS;
        int count;
        try {
            int userId = parameter.get("userId");
            User user = userDao.getUserById(userId);
            int productId = parameter.get("productId");
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