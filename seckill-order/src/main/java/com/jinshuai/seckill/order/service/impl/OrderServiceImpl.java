package com.jinshuai.seckill.order.service.impl;

import com.jinshuai.seckill.order.dao.OrderDao;
import com.jinshuai.seckill.order.entity.Order;
import com.jinshuai.seckill.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author: JS
 * @date: 2019/7/2
 * @description:
 */
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderDao orderDao;

    @Override
    public Order getOrderById(long orderId) {

        return orderDao.getOrderById(orderId);
    }

}