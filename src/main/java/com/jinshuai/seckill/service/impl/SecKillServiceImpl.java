package com.jinshuai.seckill.service.impl;

import com.jinshuai.seckill.dao.ISecKillDao;
import com.jinshuai.seckill.entity.Order;
import com.jinshuai.seckill.entity.Product;
import com.jinshuai.seckill.service.ISecKillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author: JS
 * @date: 2018/6/3
 * @description:
 */
@Service
public class SecKillServiceImpl implements ISecKillService {


    @Autowired
    private ISecKillDao secKillDao;

    @Override
    public Product getProductById(Integer productId) {
        return secKillDao.getProductById(productId);
    }

    @Override
    public int updateStockByOptimisticLock(Product product) {
        return secKillDao.updateStockByOptimisticLock(product);
    }

    @Override
    @Transactional
    public int updateStockByPessimisticLock(Product product) {
        return secKillDao.updateStockByPessimisticLock(product);
    }

    @Override
    public int createOrder(Order order) {
        return secKillDao.createOrder(order);
    }
}
