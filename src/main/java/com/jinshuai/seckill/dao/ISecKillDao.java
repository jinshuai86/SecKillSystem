package com.jinshuai.seckill.dao;

import java.util.List;

import com.jinshuai.seckill.entity.Order;
import com.jinshuai.seckill.entity.Product;
import com.jinshuai.seckill.entity.User;
import org.springframework.stereotype.Repository;

/**
 * @author: JS
 * @date: 2018/6/3
 * @description:
 */
@Repository
public interface ISecKillDao {

    /**
     * 获取商品
     * */
    Product getProductById(int productId);

    /***
     * 获取所有商品
     */
    List<Product> getAllProducts();

    /**
     * 获取商品并加锁
     * */

    Product getAndLockProductById(int productId);

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

    /**
     * 获取用户
     * */
    User getUserById(int userId);

}
