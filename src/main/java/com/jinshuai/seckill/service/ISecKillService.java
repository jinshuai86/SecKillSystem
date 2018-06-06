package com.jinshuai.seckill.service;

import com.jinshuai.seckill.entity.Order;
import com.jinshuai.seckill.entity.Product;
import com.jinshuai.seckill.enums.StatusEnum;

import java.util.List;
import java.util.Map;
public interface ISecKillService {

    /**
     * 获取商品详细信息
     * */
    Product getProductById(Integer productId);

    /***
     * 获取所有商品
     */
    List<Product> getAllProduct();

    /**
     * 乐观锁更新库存
     * */
    StatusEnum updateStockByOptimisticLock(Map<String,Integer> parameter);

    /**
     * 悲观锁更新库存
     * */
    StatusEnum updateStockByPessimisticLock(Map<String,Integer> parameter);

    /**
     * 创建订单
     * */
    StatusEnum createOrder(Order order);

}