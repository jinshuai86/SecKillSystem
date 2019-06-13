package com.jinshuai.seckill.product.service;

import com.jinshuai.seckill.product.entity.Product;
import org.apache.ibatis.annotations.Select;

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
    Product getProductById(long productId);

    /**
     * 获取商品库存
     * */
    @Select("SELECT stock FROM product WHERE id = #{id}")
    long getStockById(long productId);

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
