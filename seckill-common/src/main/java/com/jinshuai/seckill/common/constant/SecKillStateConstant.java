package com.jinshuai.seckill.common.constant;

/**
 * @author: JS
 * @date: 2018/6/11
 * @description: 秒杀过程中的状态常量
 */
public class SecKillStateConstant {

    /***
     * 秒杀成功
     */
    public static final String SUCCESS = "1";

    /**
     * 库存不足
     */
    public static final String LOW_STOCKS = "0";

    /**
     * 重复购买
     */
    public static final String REPEAT = "-1";

    /**
     * 请求参数不完整 禁止后续请求
     */
    public static final String INCOMPLETE_ARGUMENTS = "-2";


    /**
     * 库存以扣，但是创建订单失败
     */
    public static final String ORDER_ERROR = "-3";

    /**
     * 系统异常
     */
    public static final String SYSTEM_EXCEPTION = "-4";

    /**
     * 频繁请求
     */
    public static final String FREQUENCY_REQUEST = "-5";

    /**
     * CAS更新数据库库存失败，前台可以提示用户系统太忙，稍后再试
     */
    public static final String SYSTEM_BUSY = "-6";

}