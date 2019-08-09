package com.jinshuai.seckill.order.dao;

import com.jinshuai.seckill.order.entity.Order;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * @author: JS
 * @date: 2019/5/23
 * @description:
 */
@Repository
public interface OrderDao {

    /**
     * 创建订单
     */
    @Insert("INSERT INTO `order` (orderId, userId, productId, createTime) VALUES (#{orderId}, #{user.id}, #{product.id}, #{createTime})")
    int createOrder(Order order);

    /**
     * 查询订单
     */
    @Select("SELECT * FROM `order` where id = #{order.id}")
    Order getOrderById(long orderId);

}
