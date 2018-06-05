package com.jinshuai.seckill.enums;

public enum StatusEnum {

    /**
     * 秒杀成功
     * */
    SUCCESS("SUCCESS","1"),

    /**
     * 库存不足 秒杀失败
     * */
    LOW_STOCKS("FAIL","0"),

    /**
     * 系统出错 秒杀失败
     * */
    SYSTEM_EXCEPTION("FAIL","-1"),

    /**
     * 请求参数不完整 禁止后续请求
     * */
    INCOMPLETE_ARGUMENTS("INCOMPLETE_ARGUMENTS","-2"),

    /**
     * 其他 秒杀失败
     * */
    FAIL("FAIL","999"),
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
