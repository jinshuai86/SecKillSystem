package com.jinshuai.seckill.web.response;

/**
 * @author: JS
 * @date: 2018/6/4
 * @description:
 * 响应消息
 */
public class SecKillResponse {

    /**
     * 状态，比如库存不足，参数不完整，下单成功
     * */
    private String status;

    /**
     * 对应的状态码
     * */
    private String statusCode;

    public SecKillResponse(String status, String statusCode) {
        this.status = status;
        this.statusCode = statusCode;
    }

    /**
     * 解析成JSON用，下同
     * */
    public String getStatus() {
        return status;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public SecKillResponse setStatus(String status) {
        this.status = status;
        return this;
    }

    public SecKillResponse setStatusCode(String statusCode) {
        this.statusCode = statusCode;
        return this;
    }
}
