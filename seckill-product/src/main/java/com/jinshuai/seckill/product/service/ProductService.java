package com.jinshuai.seckill.product.service;

import com.jinshuai.seckill.product.entity.Product;

import java.util.List;

/**
 * @author: JS
 * @date: 2019/5/23
 * @description:
 */
public interface ProductService {

    /**
     * 获取商品详细信息
     */
    Product getProductById(Integer productId);

    /***
     * 获取所有商品
     */
    List<Product> getAllProduct();

    /**
     * 乐观锁更新库存
     */
    int updateStockByOptimisticLock(Product product);

    /**
     * 悲观锁更新库存
     */
    int updateStockByPessimisticLock(Product product);

}
