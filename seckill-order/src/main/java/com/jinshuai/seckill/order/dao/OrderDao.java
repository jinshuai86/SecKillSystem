package com.jinshuai.seckill.order.dao;

import com.jinshuai.seckill.order.entity.Order;
import org.apache.ibatis.annotations.Insert;
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
    @Insert("INSERT INTO `order` (userId,productId,createTime) VALUES (#{user.id},#{product.id},#{createTime})")
    int createOrder(Order order);

}
