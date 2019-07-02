package com.jinshuai.seckill.service.impl;

import com.jinshuai.seckill.account.dao.UserDao;
import com.jinshuai.seckill.account.entity.User;
import com.jinshuai.seckill.common.enums.StatusEnum;
import com.jinshuai.seckill.common.exception.SecKillException;
import com.jinshuai.seckill.common.util.IdUtil;
import com.jinshuai.seckill.mq.Producer;
import com.jinshuai.seckill.order.dao.OrderDao;
import com.jinshuai.seckill.order.entity.Order;
import com.jinshuai.seckill.product.dao.ProductDao;
import com.jinshuai.seckill.product.entity.Product;
import com.jinshuai.seckill.service.SecKillService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${redis.request.duration}")
    private int requestDuration;

    @Value("${redis.request.times}")
    private long requestTimes;

    @Value("${redis.expire}")
    private int expire;

    /**
     * 分隔符
     * */
    private static final String DILEMMA = ":";

    /**
     * 购买记录集合
     *
     * 每一条购买记录会被缓存，后续判重
     * */
    private static final String SHOPPING_ITEM = "shopping" + DILEMMA + "item";

    /**
     * 应用限流
     * */
    private static final String USER_LIMIT = "user" + DILEMMA + "limit";

    /**
     * 防止缓存穿透
     * */
    private static final String PENETRATION = "penetration";

    /**
     * 乐观锁: 缓存、消息队列
     *
     */
    @Override
    public StatusEnum updateStockByOptimisticLock(Map<String, Long> parameter) throws SecKillException {
        StatusEnum status = StatusEnum.SUCCESS;
        long productId = parameter.get("productId");
        long userId = parameter.get("userId");
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            jedisContainer.set(jedis);
            // 是否重复购买
            checkRepeat(userId, productId);
            // 限制请求频率
            limitRequestTimes(userId);
            /*
            * 检查库存
            *     - 库存充足:扣库存
            *         - 更新成功:创建订单到队列
            *         - 更新失败:抛出系统太忙异常，前台可以提示用户稍后再试
            *     - 库存不足:抛出库存不足异常，返回对应JSON
            * */
            checkStock(productId);
            // 扣库存
            Product product = productDao.getProductById(productId);
            updateStock(product);
            // 创建订单
            createOrder(product, userId);
        } finally {
            jedisContainer.remove();
        }
        return status;
    }

    /**
     * 是否重复购买
     *
     */
    private void checkRepeat(long userId, long productId) throws SecKillException {
        Jedis jedis = jedisContainer.get();
        // 将用户Id和商品Id作为集合中唯一元素
        String itemKey = constructCacheKey(userId, productId);
        if (jedis.sismember(SHOPPING_ITEM, itemKey)) {
            throw new SecKillException(StatusEnum.REPEAT);
        }
    }

    /**
     * 限制用户请求频率
     * 指定时间(requestDuration)内请求次数不能超过requestTimes次
     *
     */
    private void limitRequestTimes(long userId) throws SecKillException {
        Jedis jedis = jedisContainer.get();
        // 每个用户的请求标识
        String itemKey = constructCacheKey(USER_LIMIT, userId);
        // 已经请求的次数
        String reqTimes = jedis.get(itemKey);
        // 第一次请求：设置初始值
        if (reqTimes == null) {
            jedis.set(itemKey, "1");
            jedis.expire(itemKey, requestDuration);
        }
        // 限速
        else if (Long.valueOf(reqTimes) >= requestTimes) {
            throw new SecKillException(StatusEnum.FREQUENCY_REQUEST);
        }
        // 还没超过限制次数
        else {
            jedis.incr(itemKey);
        }
    }

    /**
     * 检查缓存库存、处理缓存穿透
     *
     */
    private void checkStock(long productId) throws SecKillException {
        Jedis jedis = jedisContainer.get();
        String cacheProductKey = constructCacheKey("product", productId, "stock");
        String cacheProductStock = jedis.get(cacheProductKey);
        // 命中无意义数据
        if (PENETRATION.equals(cacheProductStock)) {
            throw new SecKillException(StatusEnum.INCOMPLETE_ARGUMENTS);
        }
        // 缓存未命中
        if (cacheProductStock == null) {
            Product product = productDao.getProductById(productId);
            // 数据库不存在此商品
            if (product == null) {
                // 通过缓存没意义的数据防止缓存穿透
                jedis.set(cacheProductKey, PENETRATION);
                throw new SecKillException(StatusEnum.INCOMPLETE_ARGUMENTS);
            } else {
                cacheProductStock = String.valueOf(product.getStock());
                jedis.set(cacheProductKey, cacheProductStock);
                jedis.expire(cacheProductKey, expire);
            }
        }
        // 库存不足
        if (Long.valueOf(cacheProductStock) == 0) {
            throw new SecKillException(StatusEnum.LOW_STOCKS);
        }
    }

    /**
     * 扣库存、删除缓存
     *
     */
    private void updateStock(Product product) throws SecKillException {
        Jedis jedis = jedisContainer.get();
        String cacheProductStockKey = constructCacheKey("product", product.getId(), "stock");
        // 更新数据库商品库
        if (product.getStock() == 0) {
            throw new SecKillException(StatusEnum.LOW_STOCKS);
        } else {
            int count = productDao.updateStockByOptimisticLock(product);
            if (count != 1) {
                throw new SecKillException(StatusEnum.SYSTEM_BUSY);
            } else {
                jedis.del(cacheProductStockKey);
            }
        }
    }

    /**
     * 订单入队列
     *
     */
    private void createOrder(Product product, long userId) throws SecKillException {
        User user = userDao.getUserById(userId);
        if (user == null) {
            throw new SecKillException(StatusEnum.INCOMPLETE_ARGUMENTS);
        }
        Jedis jedis = jedisContainer.get();
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        Order order = new Order(user, product, ts, IdUtil.nextId());
        orderProducer.product(order);
        // 缓存购买记录，防止重复购买, 以下代码如果抛异常就会出现超卖，如果抛出异常后就会回滚扣库存的SQL，但是订单消息已经放到队列
        // TODO 剥离到事务外
        String itemKey = constructCacheKey(user.getId(), product.getId());
        jedis.sadd(SHOPPING_ITEM, itemKey);
    }

    /**
     * 构造缓存Key
     * */
    private String constructCacheKey(Object ...args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]);
            if (i != args.length - 1)
                sb.append(DILEMMA);
        }
        return sb.toString();
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
                    createOrder(product, userId);
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
                    Order order = new Order(user, product, ts, IdUtil.nextId());
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