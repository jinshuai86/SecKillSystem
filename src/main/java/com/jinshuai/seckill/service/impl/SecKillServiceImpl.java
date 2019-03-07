package com.jinshuai.seckill.service.impl;

import com.jinshuai.seckill.dao.ISecKillDao;
import com.jinshuai.seckill.entity.Order;
import com.jinshuai.seckill.entity.Product;
import com.jinshuai.seckill.entity.User;
import com.jinshuai.seckill.enums.StatusEnum;
import com.jinshuai.seckill.exception.SecKillException;
import com.jinshuai.seckill.mq.Producer;
import com.jinshuai.seckill.service.ISecKillService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author: JS
 * @date: 2018/6/3
 * @description:
 */
@Service
@Slf4j
public class SecKillServiceImpl implements ISecKillService {

    @Autowired
    private ISecKillDao secKillDao;

    @Autowired
    private JedisSentinelPool jedisPool;

    @Autowired
    private Producer<Order> orderProducer;

    @Override
    public Product getProductById(Integer productId) {
        return secKillDao.getProductById(productId);
    }

    @Override
    public List<Product> getAllProduct() {
        return secKillDao.getAllProducts();
    }

    /**
     * 乐观锁：加缓存、加消息队列
     * */
    @Override
    public StatusEnum updateStockByOptimisticLock(Map<String,Integer> parameter) {
        @SuppressWarnings("unchecked")
        StatusEnum status = StatusEnum.SUCCESS;
        int productId = parameter.get("productId");
        int userId = parameter.get("userId");
        User user = secKillDao.getUserById(userId);
        // 检查 -> 更新为非原子操作：可以在更新缓存库存以后检查缓存库存，如果是负数则更新失败，并将缓存库存恢复为0
        try (Jedis jedis = jedisPool.getResource()) { // 为了关闭jedis
            // TODO 没有写ProductDao，临时设置的Product- -!!
            Product product = new Product();
            product.setId(productId);
            // 查看是否重复购买
            checkRepeat(user, product, jedis);
            // 限制请求次数
            limitRequestTimes(user, product, jedis);
            // 查看缓存库存
            checkStock(product, jedis);
            // 构造版本号
            String cacheProductVersionKey = "product:" + productId + ":version";
            String versionStr = jedis.get(cacheProductVersionKey);
            int version = Integer.valueOf(versionStr);
            product.setVersion(version);
            // 更新库存
            updateStock(product, jedis);
            // 创建订单
            createOrder(product, user, jedis);
        } catch (SecKillException e) {
            throw e;
        }
        return status;
    }

    /**
     * 查看是否重复购买
     * */
    private void checkRepeat(User user, Product product, Jedis jedis) {
        // 将用户Id和商品Id作为集合中唯一元素
        String itemKey = String.valueOf(user.getId()) + product.getId();
        if (jedis.sismember("item",itemKey)) {
            throw new SecKillException(StatusEnum.REPEAT);
        }
    }

    /**
     * 限速：用户1秒内请求次数不能超过5次
     * */
    private void limitRequestTimes(User user, Product product, Jedis jedis) {
        // 将用户Id和商品Id作为key
        String itemKey = String.valueOf(user.getId()) + product.getId();
        String reqTimes = jedis.get(itemKey);
        // 第一次请求：设置初始值
        if (reqTimes == null) {
            jedis.set(itemKey,"1");
            jedis.expire(itemKey,5);
        }
        // 限速
        else if (Integer.valueOf(reqTimes) > 5) {
            log.warn("用户[{}]频繁请求商品[{}]",user.getId(),product.getId());
            throw new SecKillException(StatusEnum.FREQUENCY_REQUEST);
        }
        // 还没超过限制次数
        else {
            jedis.incr(itemKey);
        }
    }

    /**
     * 1. 处理缓存穿透 2. 检查库存
     * */
    private void checkStock(Product product, Jedis jedis) {
        int productId = product.getId();
        String cacheProductKey = "product:" + productId + ":stock";
        String stockStr = jedis.get(cacheProductKey);
        // 缓存未命中
        if (stockStr == null) {
            log.warn("商品编号: [{}] 未在缓存命中",productId);
            // 在存储层去查找
            product = secKillDao.getProductById(productId);
            // 存储层不存在此商品
            if (product == null) {
                log.warn("商品编号: [{}] 未在存储层命中,已添加冗余数据到缓存",productId);
                // 通过缓存没意义的数据防止缓存穿透
                jedis.set(cacheProductKey,"penetration");
                throw new SecKillException(StatusEnum.INCOMPLETE_ARGUMENTS);
            }
            // 存储层存在此商品，添加到缓存：先判断再修改会导致并发修改不安全，通过加锁避免
            else {
                synchronized (this.getClass()) {
                    // 如果没有在缓存中设置此商品，再设置
                    if (jedis.get(cacheProductKey) == null) {
                        jedis.set(cacheProductKey,String.valueOf(product.getStock()));
                    }
                    // 检查此商品在缓存里的库存
                    checkCacheStock(productId, jedis);
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
                checkCacheStock(productId, jedis);
            }
        }
    }

    /**
     * 查看缓存中的此商品的数量
     * */
    private void checkCacheStock(int productId, Jedis jedis) {
        String cacheProductKey = "product:" + productId + ":stock";
        String stockStr = jedis.get(cacheProductKey);
        int cacheStock = Integer.valueOf(stockStr);
        if (cacheStock == 0) {
            throw new SecKillException(StatusEnum.LOW_STOCKS);
        }
    }

    /**
     * 更新缓存库存和数据库库存
     * */
    private void updateStock(Product product, Jedis jedis) {
        int productId = product.getId();
        String cacheProductVersionKey = "product:" + productId + ":version";
        String cacheProductStockKey = "product:" + productId + ":stock";
        // 更新缓存库存
        long currentCacheStock = jedis.decr(cacheProductStockKey);
        // 防止并发修改导致超卖
        if (currentCacheStock < 0) {
            jedis.set(cacheProductStockKey,String.valueOf(0));
            throw new SecKillException(StatusEnum.LOW_STOCKS);
        } else {
            // 更新数据库商品库存
            int count = secKillDao.updateStockByOptimisticLock(product);
            if (count != 1) {
                // 更新数据库商品库存失败，回滚之前修改的缓存库存
                jedis.incr(cacheProductStockKey);
                throw new SecKillException(StatusEnum.LOW_STOCKS);
            }
            // 更新缓存版本号
            jedis.incr(cacheProductVersionKey);
        }
    }

    /**
     * 创建订单
     * */
    private void createOrder(Product product, User user, Jedis jedis) {
        DateTime dateTime = new DateTime();
        Timestamp ts = new Timestamp(dateTime.getMillis());
        Order order = new Order(user,product,ts, UUID.randomUUID().toString());
        // 放到消息队列 TODO 可以提示用户正在排队中... ...
        orderProducer.product(order);
        // 放到数据库
//         int count = secKillDao.createOrder(order);
//        if (count != 1) {
//            // 此时库存已经扣除 TODO 是否需要回滚数据？比如socket timeout... ...蓝廋
//            throw new SecKillException(StatusEnum.ORDER_ERROR);
//        }
        // 添加到购买记录
        String itemKey = user.getId() + "" + product.getId();
        jedis.sadd("item",itemKey);
    }

    /**
     * 乐观锁：未加缓存
     * */
//  @Override
    public StatusEnum _updateStockByOptimisticLock(Map<String,Integer> parameter) {
        StatusEnum status = StatusEnum.SUCCESS;
        int productId = parameter.get("productId");
        Product product = secKillDao.getProductById(productId);
        int userId = parameter.get("userId");
        User user = secKillDao.getUserById(userId);
        try {
            // 更新/修改成功标志位
            int count = 0;
            // 检查库存
            if (product.getStock() > 0) {
                /* 更新库存 TODO:锁库存而不是更新库存：创建订单失败，此次操作需要回滚。 */
                count = secKillDao.updateStockByOptimisticLock(product);
                /* 更新库存失败 TODO:进行自旋重新尝试购买 */
                if (count != 1) {
                    status = StatusEnum.SYSTEM_EXCEPTION;
                } else {
                    // 创建订单
                    createOrder(product,user,jedisPool.getResource());
                }
            } else { // 库存不足
                status = StatusEnum.LOW_STOCKS;
                log.warn("库存不足 productId： [{}] productName：[{}]",productId,product.getProductName());
            }
        } catch (Exception e) {
            log.error("秒杀失败",e);
            status = StatusEnum.SYSTEM_EXCEPTION;
        }
        return status;
    }

    /**
     * 悲观锁
     * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public StatusEnum updateStockByPessimisticLock(Map<String,Integer> parameter) {
        StatusEnum status = StatusEnum.SUCCESS;
        int count;
        try {
            int userId = parameter.get("userId");
            User user = secKillDao.getUserById(userId);
            int productId = parameter.get("productId");
            Product product = secKillDao.getAndLockProductById(productId);
            // 库存充足
            if (product.getStock() > 0) {
                // 更新库存
                count = secKillDao.updateStockByPessimisticLock(product);
                // 更新失败
                if (count != 1) {
                    status = StatusEnum.SYSTEM_EXCEPTION;
                } else {
                    // 创建订单
                    DateTime dateTime = new DateTime();
                    Timestamp ts = new Timestamp(dateTime.getMillis());
                    Order order = new Order(user,product,ts,UUID.randomUUID().toString());
                    secKillDao.createOrder(order);
                }
            } else { // 库存不足
                status = StatusEnum.LOW_STOCKS;
                log.warn("库存不足 productId： [{}] productName：[{}]", productId, product.getProductName());
            }
        } catch (Exception e) {
            log.error("创建订单失败",e);
            status = StatusEnum.SYSTEM_EXCEPTION;
        }
        return status;
    }

}