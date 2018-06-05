package com.jinshuai.seckill.web;

/**
 * @author: JS
 * @date: 2018/6/4
 * @description:
 *
 */
public class SecKillRequest {

    private int userId;

    private int productId;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }
}
