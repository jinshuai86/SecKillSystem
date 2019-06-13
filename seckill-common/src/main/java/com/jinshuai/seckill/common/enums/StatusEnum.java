package com.jinshuai.seckill.common.enums;

import com.jinshuai.seckill.common.constant.SecKillStateConstant;

/**
 * 描述请求过程中的状态信息，作为自定义异常类的构造参数。
 */
public enum StatusEnum {

    /**
     * 秒杀成功
     */
    SUCCESS("秒杀成功", SecKillStateConstant.SUCCESS),

    /**
     * 库存不足
     */
    LOW_STOCKS("库存不足", SecKillStateConstant.LOW_STOCKS),

    /**
     * 重复购买
     */
    REPEAT("不可重复购买", SecKillStateConstant.REPEAT),

    /**
     * 请求参数不合法 {1. 不完整 2. 故意不命中缓存}
     */
    INCOMPLETE_ARGUMENTS("请求参数不合法", SecKillStateConstant.INCOMPLETE_ARGUMENTS),

    /**
     * 订单入队列失败
     */
    ORDER_ERROR("创建订单异常", SecKillStateConstant.ORDER_ERROR),

    /**
     * 系统出错
     */
    SYSTEM_EXCEPTION("系统异常", SecKillStateConstant.SYSTEM_EXCEPTION),

    /**
     * 频繁请求
     */
    FREQUENCY_REQUEST("频繁请求", SecKillStateConstant.FREQUENCY_REQUEST),

    /**
     * 乐观锁更新数据库失败，系统太忙
     * */
    SYSTEM_BUSY("系统太忙", SecKillStateConstant.SYSTEM_BUSY),
    ;

    private String status;
    private String statusCode;

    StatusEnum(String status, String statusCode) {
        this.status = status;
        this.statusCode = statusCode;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusCode() {
        return statusCode;
    }
}
