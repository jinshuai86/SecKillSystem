package com.jinshuai.seckill.entity;

import org.joda.time.DateTime;

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
    private Integer id;

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
    private DateTime createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public DateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(DateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Order order = (Order) o;

        if (id != null ? !id.equals(order.id) : order.id != null) return false;
        if (user != null ? !user.equals(order.user) : order.user != null) return false;
        if (product != null ? !product.equals(order.product) : order.product != null) return false;
        return createTime != null ? createTime.equals(order.createTime) : order.createTime == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (product != null ? product.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        return result;
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