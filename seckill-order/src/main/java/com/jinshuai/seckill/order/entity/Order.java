package com.jinshuai.seckill.order.entity;

import com.jinshuai.seckill.account.entity.User;
import com.jinshuai.seckill.product.entity.Product;
import lombok.Data;

import java.sql.Timestamp;

/**
 * @author: JS
 * @date: 2018/6/3
 * @description: TODO 暂时没有考虑范式规范
 */
@Data
public class Order {
    /**
     * 订单自增ID
     */
    private int id;

    /**
     * 订单唯一业务标识
     */
    private long orderId;

    /**
     * 订单所属用户
     */
    private User user;

    /**
     * 订单所含产品
     */
    private Product product;

    /**
     * 订单创建时间
     */
    private Timestamp createTime;

    Order() {
    }

    public Order(User user, Product product, Timestamp createTime, long orderId) {
        this.user = user;
        this.product = product;
        this.createTime = createTime;
        this.orderId = orderId;
    }
}
