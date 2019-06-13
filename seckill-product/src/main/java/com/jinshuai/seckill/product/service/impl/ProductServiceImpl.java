package com.jinshuai.seckill.product.service.impl;

import com.jinshuai.seckill.common.annotation.TargetDataSource;
import com.jinshuai.seckill.common.constant.DataSourceConstant;
import com.jinshuai.seckill.product.dao.ProductDao;
import com.jinshuai.seckill.product.entity.Product;
import com.jinshuai.seckill.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

import java.util.List;

/**
 * @author: JS
 * @date: 2019/5/23
 * @description:
 */
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductDao productDao;

    @Autowired
    private JedisSentinelPool jedisSentinelPool;

    @TargetDataSource(DataSourceConstant.SLAVE)
    @Override
    public long getStockById(long productId) {
        return productDao.getStockById(productId);
    }

    @TargetDataSource(DataSourceConstant.SLAVE)
    @Override
    public Product getProductById(long productId) {
        return productDao.getProductById(productId);
    }

    @TargetDataSource(DataSourceConstant.SLAVE)
    @Override
    public List<Product> getAllProduct() {
        return productDao.getAllProducts();
    }

    @Override
    public int updateStockByOptimisticLock(Product product) {
        int res = productDao.updateStockByOptimisticLock(product);
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            jedis.del("product:" + product.getId() + ":stock");
        } catch (Throwable e) {
            throw e;
        }
        return res;
    }

    @Override
    public int updateStockByPessimisticLock(Product product) {
        int res = productDao.updateStockByOptimisticLock(product);
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            jedis.del("product:" + product.getId() + ":stock");
        } catch (Throwable e) {
            throw e;
        }
        return res;
    }
}
