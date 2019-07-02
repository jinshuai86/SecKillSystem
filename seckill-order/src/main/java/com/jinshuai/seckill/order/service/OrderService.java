package com.jinshuai.seckill.order.service;

import com.jinshuai.seckill.order.entity.Order;

/**
 * @author: JS
 * @date: 2019/7/2
 * @description:
 */
public interface OrderService {

    Order getOrderById(long orderId);

}
