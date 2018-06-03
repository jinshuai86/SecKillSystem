package com.jinshuai.seckill.controller;

import com.jinshuai.seckill.service.ISecKillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: JS
 * @date: 2018/6/3
 * @description:
 * 秒杀请求接口
 */
@RestController
public class SecKillController {

    @Autowired
    private ISecKillService secKillService;

    /**
     * 乐观锁
     * */


}