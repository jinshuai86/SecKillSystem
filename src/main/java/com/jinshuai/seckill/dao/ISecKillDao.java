package com.jinshuai.seckill.dao;

import com.jinshuai.seckill.entity.Order;
import com.jinshuai.seckill.entity.Product;
import org.springframework.stereotype.Repository;

/**
 * @author: JS
 * @date: 2018/6/3
 * @description:
 */
@Repository
public interface ISecKillDao {

    /**
     * 获取商品详细信息
     * */
    Product getProductById(Integer productId);

    /**
     * 乐观锁更新库存
     * */
    int updateStockByOptimisticLock(Product product);

    /**
     * 悲观锁更新库存
     * */
    int updateStockByPessimisticLock(Product product);

    /**
     * 创建订单
     * */
    int createOrder(Order order);
}
