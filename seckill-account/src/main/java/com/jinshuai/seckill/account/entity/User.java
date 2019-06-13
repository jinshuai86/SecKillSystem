package com.jinshuai.seckill.account.entity;

import lombok.Data;

/**
 * @author: JS
 * @date: 2018/6/3
 * @description: robot
 */
@Data
public class User {

    /**
     * 用户id
     */
    private long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 手机号
     */
    private String phone;

}
