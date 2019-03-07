package com.jinshuai.seckill.entity;

import lombok.Data;

import java.sql.Timestamp;

/**
 * @author: JS
 * @date: 2018/6/3
 * @description:
 * TODO 暂时没有考虑范式规范
 */
@Data
public class Order {
    /**
     * 订单ID
     * */
    private int id;

    /**
     * 订单唯一标识
     * */
    private String orderUUID;

    /**
     * 订单所属用户
     * */
    private User user;

    /**
     * 订单所含产品
     * */
    private Product product;

    /**
     * 订单创建时间
     * */
    private Timestamp createTime;

    Order(){}

    public Order(User user, Product product, Timestamp createTime, String orderUUID) {
        this.user = user;
        this.product = product;
        this.createTime = createTime;
        this.orderUUID = orderUUID;
    }
}