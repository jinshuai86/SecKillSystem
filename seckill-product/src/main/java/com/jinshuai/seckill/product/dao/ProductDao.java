package com.jinshuai.seckill.product.dao;

import com.jinshuai.seckill.product.entity.Product;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author: JS
 * @date: 2019/5/23
 * @description:
 */
@Repository
public interface ProductDao {

    /**
     * 获取商品快照
     */
    @Select("SELECT * FROM product WHERE id = #{id}")
    Product getProductById(long productId);

    /**
     * 获取商品库存
     * */
    @Select("SELECT stock FROM product WHERE id = #{id}")
    long getStockById(long productId);

    /**
     * 获取商品并加锁
     */
    @Select("SELECT * FROM product WHERE id = #{id} FOR UPDATE")
    Product getAndLockProductById(long productId);

    /***
     * 获取所有商品
     */
    @Select("SELECT id,stock,version FROM product")
    List<Product> getAllProducts();

    /**
     * 乐观锁更新库存
     */
    @Update("UPDATE product SET stock = stock - 1,version = version + 1 WHERE id = #{id} AND version = #{version}")
    int updateStockByOptimisticLock(Product product);

    /**
     * 悲观锁更新库存
     */
    @Update("UPDATE product SET stock = stock - 1 WHERE id = #{id}")
    int updateStockByPessimisticLock(Product product);

}
