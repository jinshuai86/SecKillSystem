package com.jinshuai.seckill.exception;

import com.jinshuai.seckill.enums.StatusEnum;
import lombok.Data;

/**
 * @author: JS
 * @date: 2018/6/11
 * @description: 通过此类进行构造秒杀过程中产生的异常
 */
@Data
public class SecKillException extends RuntimeException{

    private StatusEnum statusEnum;

    public SecKillException(StatusEnum statusEnum) {
        this.statusEnum = statusEnum;
    }

}