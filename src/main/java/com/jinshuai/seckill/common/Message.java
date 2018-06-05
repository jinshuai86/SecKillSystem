package com.jinshuai.seckill.common;

/**
 * @author: JS
 * @date: 2018/6/4
 * @description:
 * 响应消息
 */
public class Message {

    private String status;
    private String statusCode;

    public Message(String status, String statusCode) {
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

    public Message setStatus(String status) {
        this.status = status;
        return this;
    }

    public Message setStatusCode(String statusCode) {
        this.statusCode = statusCode;
        return this;
    }
}
