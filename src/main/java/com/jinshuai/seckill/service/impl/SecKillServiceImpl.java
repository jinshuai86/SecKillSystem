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
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
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

    @Override
    public Product getProductById(Integer productId) {
        return secKillDao.getProductById(productId);
    }

    /**
     * 乐观锁
     * */
    @Override
    public StatusEnum updateStockByOptimisticLock(Map<String,Integer> parameter) {
        StatusEnum status = StatusEnum.SUCCESS;
        // 持久层操作返回信息
        int response = 0;
        try {
            int productId = parameter.get("productId");
            Product product = secKillDao.getProductById(productId);
            int userId = parameter.get("userId");
            User user = secKillDao.getUserById(userId);
            // 库存充足
            if (product.getStock() > 0) {
                /* 更新库存 TODO:锁库存而不是更新库存，有可能创建订单失败，此次操作需要回滚。 */
                response = secKillDao.updateStockByOptimisticLock(product);
                /* 更新库存失败 TODO:进行自旋重新尝试购买 */
                if (response != 1) {
                    status = StatusEnum.FAIL;
                } else {
                    // 创建订单
                    DateTime dateTime = new DateTime();
                    Timestamp ts = new Timestamp(dateTime.getMillis());
                    Order order = new Order(user,product,ts);
                    response = secKillDao.createOrder(order);
                    // 创建订单失败
                    if (response != 1) {
                        status = StatusEnum.FAIL;
                    }
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
    @Transactional
    public StatusEnum updateStockByPessimisticLock(Map<String,Integer> parameter) {
        StatusEnum status = StatusEnum.SUCCESS;
        int response = 0;
        try {
            int userId = parameter.get("userId");
            User user = secKillDao.getUserById(userId);
            int productId = parameter.get("productId");
            Product product = secKillDao.getAndLockProductById(productId);
            // 库存充足
            if (product.getStock() > 0) {
                // 更新库存
                response = secKillDao.updateStockByPessimisticLock(product);
                // 更新失败
                if (response != 1) {
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

    @Override
    public StatusEnum createOrder(Order order) {
        secKillDao.createOrder(order);
        return null;
    }
}
