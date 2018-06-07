package com.jinshuai.seckill.service.impl;

import com.jinshuai.seckill.dao.ISecKillDao;
import com.jinshuai.seckill.entity.Order;
import com.jinshuai.seckill.entity.Product;
import com.jinshuai.seckill.entity.User;
import com.jinshuai.seckill.enums.StatusEnum;
import com.jinshuai.seckill.service.ISecKillService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * @author: JS
 * @date: 2018/6/3
 * @description:
 */
@Service
public class SecKillServiceImpl implements ISecKillService {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ISecKillDao secKillDao;

    @Autowired
    private JedisPool jedisPool;

    @Override
    public Product getProductById(Integer productId) {
        return secKillDao.getProductById(productId);
    }

    @Override
    public List<Product> getAllProduct() {
        return secKillDao.getAllProducts();
    }

    /**
     * 乐观锁：加缓存
     * */
    @Override
    public StatusEnum updateStockByOptimisticLock(Map<String,Integer> parameter) {
        StatusEnum status = StatusEnum.SUCCESS;
        int productId = parameter.get("productId");
        int userId = parameter.get("userId");
        User user = secKillDao.getUserById(userId);
        // 检查 -> 更新为非原子操作：可以在更新缓存库存以后检查缓存库存，如果是负数则更新失败，并将缓存库存恢复为0
        try (Jedis jedis = jedisPool.getResource()) {
            Product product = new Product();
            product.setId(productId);
            // 检查缓存库存
            status = checkStock(product,status,jedis);
            if (!status.equals(StatusEnum.LOW_STOCKS) && !status.equals(StatusEnum.INCOMPLETE_ARGUMENTS)) {
                // 放到if后预防缓存穿透
                String cacheProductVersionKey = "product:" + productId + ":version";
                int version = Integer.valueOf(jedis.get(cacheProductVersionKey));
                product.setVersion(version);
                // 更新库存
                updateStock(product,jedis);
                // 创建订单
                createOrder(product,user);
            }
        } catch (Exception e) {
            LOGGER.error("系统异常",e);
            status = StatusEnum.SYSTEM_EXCEPTION;
        }
        return status;
    }

    /**
     * 1. 处理缓存穿透 2. 检查库存
     * */
    private final StatusEnum checkStock(Product product, StatusEnum status,Jedis jedis) {
        int productId = product.getId();
        String cacheProductKey = "product:" + productId + ":stock";
        String stockStr = jedis.get(cacheProductKey);
        // 缓存未命中
        if (stockStr == null) {
            // 在存储层去查找
            product = secKillDao.getProductById(productId);
            // 存储层不存在此商品
            if (product == null) {
                // 通过缓存没意义的数据防止缓存穿透
                jedis.set(cacheProductKey,"penetration");
            } else {
                // 存储层存在此商品，添加到缓存
                jedis.set(cacheProductKey,String.valueOf(product.getStock()));
                // 检查此商品在缓存里的库存
                status = checkCacheStock(productId,status,jedis);
            }
        } else {
            // 命中无意义数据
            if ("penetration".equals(stockStr)) {
                LOGGER.error("请求数据不合法 productId : [{}]",productId);
                status = StatusEnum.INCOMPLETE_ARGUMENTS;
            } else {
                // 检查缓存库存
                status = checkCacheStock(productId,status,jedis);
            }
        }
        return status;
    }

    private final StatusEnum checkCacheStock(int productId, StatusEnum status, Jedis jedis) {
        String cacheProductKey = "product:" + productId + ":stock";
        String stockStr = jedis.get(cacheProductKey);
        int cacheStock = Integer.valueOf(stockStr);
        if (cacheStock == 0) {
            status = StatusEnum.LOW_STOCKS;
            LOGGER.warn("库存不足 productId： [{}]", productId);
        }
        return status;
    }

    /**
     * 更新缓存库存和数据库库存
     * */
    private final void updateStock(Product product, Jedis jedis) {
        int productId = product.getId();
        String cacheProductVersionKey = "product:" + productId + ":version";
        String cacheProductStockKey = "product:" + productId + ":stock";
        // 更新缓存库存
        long currentCacheStock = jedis.decr(cacheProductStockKey);
        // 防止并发修改导致超卖
        if (currentCacheStock < 0) {
            jedis.set(cacheProductStockKey,String.valueOf(0));
            throw new RuntimeException("productId:[" +productId+ "]库存不足，更新缓存失败");
        } else {
            // 更新数据库商品库存
            int count = secKillDao.updateStockByOptimisticLock(product);
            if (count != 1) {
                // 更新数据库商品库存失败，回滚之前修改的缓存库存
                jedis.incr(cacheProductStockKey);
                throw new RuntimeException("productId:[" +productId+ "]，更新库存失败,当前库存：" + product.getStock());
            }
            // 更新缓存版本号
            jedis.incr(cacheProductVersionKey);
        }
    }

    /**
     * 创建订单
     * */
    private void createOrder(Product product, User user) {
        DateTime dateTime = new DateTime();
        Timestamp ts = new Timestamp(dateTime.getMillis());
        Order order = new Order(user,product,ts);
        int count = secKillDao.createOrder(order);
        if (count != 1) {
            // 此时库存已经扣除
            LOGGER.error("userId[{}] productId[{}] 创建订单失败"); // 查看指定类型的日志，避免订单异常。
            throw new RuntimeException("userId[" + user.getId() + "] " +"productId[" + product.getId() +"] 创建订单失败");
        }
    }

    /**
     * 乐观锁：未加缓存
     * */
//    @Override
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
                    status = StatusEnum.FAIL;
                } else {
                    // 创建订单
                    createOrder(product,user);
                }
            } else { // 库存不足
                status = StatusEnum.LOW_STOCKS;
                LOGGER.warn("库存不足 productId： [{}] productName：[{}]",productId,product.getProductName());
            }
        } catch (Exception e) {
            LOGGER.error("秒杀失败",e);
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
        int count = 0;
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
                    status = StatusEnum.FAIL;
                } else {
                    // 创建订单
                    DateTime dateTime = new DateTime();
                    Timestamp ts = new Timestamp(dateTime.getMillis());
                    Order order = new Order(user,product,ts);
                    secKillDao.createOrder(order);
                }
            } else { // 库存不足
                status = StatusEnum.LOW_STOCKS;
                LOGGER.warn("库存不足 productId： [{}] productName：[{}]", productId, product.getProductName());
            }
        } catch (Exception e) {
            LOGGER.error("创建订单失败",e);
            status = StatusEnum.SYSTEM_EXCEPTION;
        }
        return status;
    }

}