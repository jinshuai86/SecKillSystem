package com.jinshuai.seckill.entity;

import java.sql.Timestamp;

/**
 * @author: JS
 * @date: 2018/6/3
 * @description:
 * 暂时没有考虑范式规范
 */
public class Order {
    /**
     * 订单ID
     * */
    private int id;

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

    public Order(){}

    public Order(int id, User user, Product product, Timestamp createTime) {
        this.id = id;
        this.user = user;
        this.product = product;
        this.createTime = createTime;
    }

    public Order(User user, Product product, Timestamp createTime) {
        this.user = user;
        this.product = product;
        this.createTime = createTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", user=" + user +
                ", product=" + product +
                ", createTime=" + createTime +
                '}';
    }
}