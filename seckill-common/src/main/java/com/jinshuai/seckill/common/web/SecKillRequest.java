package com.jinshuai.seckill.common.web;

/**
 * @author: JS
 * @date: 2018/6/4
 * @description: 组装请求JSON为SecKillRequest对象
 */
public class SecKillRequest {

    private Integer userId;

    private Integer productId;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    @Override
    public String toString() {
        return "SecKillRequest{" +
                "userId=" + userId +
                ", productId=" + productId +
                '}';
    }
}
