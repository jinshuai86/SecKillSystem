package com.jinshuai.seckill.dao;

import java.util.List;

import com.jinshuai.seckill.entity.Order;
import com.jinshuai.seckill.entity.Product;
import com.jinshuai.seckill.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
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
    @Select("SELECT * FROM product WHERE id = #{id}")
    Product getProductById(int productId);

    /***
     * 获取所有商品
     */
    @Select("SELECT id,stock,version FROM product")
    List<Product> getAllProducts();

    /**
     * 获取商品并加锁
     * */
    @Select("SELECT * FROM product WHERE id = #{id} FOR UPDATE")
    Product getAndLockProductById(int productId);

    /**
     * 乐观锁更新库存
     * */
    @Update("UPDATE product SET stock = stock - 1,version = version + 1 WHERE id = #{id} AND version = #{version}")
    int updateStockByOptimisticLock(Product product);

    /**
     * 悲观锁更新库存
     * */
    @Update("UPDATE product SET stock = stock - 1 WHERE id = #{id}")
    int updateStockByPessimisticLock(Product product);

    /**
     * 创建订单
     * */
    @Insert("INSERT INTO `order` (userId,productId,createTime) VALUES (#{user.id},#{product.id},#{createTime})")
    int createOrder(Order order);

    /**
     * 获取用户
     * */
    @Select("SELECT * FROM user WHERE id = #{id}")
    User getUserById(int userId);

}
