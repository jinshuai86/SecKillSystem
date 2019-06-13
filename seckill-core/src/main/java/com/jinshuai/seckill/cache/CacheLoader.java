package com.jinshuai.seckill.cache;

import com.alibaba.fastjson.JSON;
import com.jinshuai.seckill.product.dao.ProductDao;
import com.jinshuai.seckill.product.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author: JS
 * @date: 2018/6/6
 * @description: 初始化缓存
 */
@Component
@Slf4j
public class CacheLoader {

    @Autowired
    private ProductDao productDao;

    @Autowired
    private JedisPool jedisPool;

    /**
     * 将存储层产品信息加载到缓存中
     */
    @PostConstruct
    private void initCache() {

        try (Jedis jedis = jedisPool.getResource()) {
            // 清除旧的缓存
            jedis.flushDB();
            // 通过管道执行批处理
            Pipeline pipeline = jedis.pipelined();
            List<Product> productList = productDao.getAllProducts();
            productList.forEach(product -> {
                pipeline.set("product:" + product.getId(), JSON.toJSONString(product));
                pipeline.expire("product:" + product.getId(), (int)(Math.random() * 120000));
            });
            pipeline.sync();
            log.info("商品库存、版本号已加载到缓存中！！！");
        } catch (Exception e) {
            log.error("加载商品到缓存出错,正在停止JVM......", e);
            System.exit(-1);
        }

    }

}