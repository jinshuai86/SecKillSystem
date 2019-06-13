package com.jinshuai.seckill.common.web;

/**
 * @author: JS
 * @date: 2018/6/4
 * @description: 组装请求JSON为SecKillRequest对象
 */
public class SecKillRequest {

    private Long userId;

    private Long productId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
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
