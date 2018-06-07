package com.jinshuai.seckill.cache;

import java.util.List;

import com.jinshuai.seckill.dao.ISecKillDao;
import com.jinshuai.seckill.entity.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import javax.annotation.PostConstruct;

/**
 * @author: JS
 * @date: 2018/6/6
 * @description:
 * 初始化缓存
 */
@Component
public class CacheLoader {

    private final Logger LOGGER = LoggerFactory.getLogger(CacheLoader.class);

    @Autowired
    private ISecKillDao secKillDao;

    @Autowired
    private JedisPool jedisPool;

    /**
     * 将存储层产品信息加载到缓存中
     * */
    @PostConstruct
    private void initCache() {
        try {
            Jedis jedis = jedisPool.getResource();
            jedis.flushDB();
            Pipeline pipeline = jedis.pipelined();
            List<Product> productList = secKillDao.getAllProducts();
            productList.forEach(product -> {
                pipeline.set("product:" + product.getId() + ":stock", String.valueOf(product.getStock()));
                pipeline.set("product:" + product.getId() + ":version", String.valueOf(product.getVersion()));
            });
            pipeline.sync();
            LOGGER.info("商品库存、版本号已加载到缓存中！！！");
        } catch (Exception e) {
            LOGGER.error("加载商品到缓存出错,正在停止JVM......",e);
            System.exit(-1);
        }

    }

}