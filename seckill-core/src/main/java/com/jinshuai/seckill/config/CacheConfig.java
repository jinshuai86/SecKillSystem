package com.jinshuai.seckill.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author: JS
 * @date: 2018/6/6
 * @description: use redis as cache
 *
 */
@Configuration
public class CacheConfig {

    @Value("${spring.redis.port}")
    private int port;

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.password}")
    private String password;

    @Value("${spring.redis.timeout}")
    private int timeout;

    @Value("${spring.redis.jedis.pool.max-active}")
    private int maxActive;

    @Value("${spring.redis.jedis.pool.max-idle}")
    private int maxIdle;

    @Value("${spring.redis.jedis.pool.min-idle}")
    private int minIdle;

    @Value("${spring.redis.jedis.pool.max-wait}")
    private int maxWait;

    @Value("${spring.redis.sentinel.master}")
    private String clusterName;

    @Value("${spring.redis.sentinel.nodes}")
    private String sentinelNodes;


    @Bean(name = "jedisPoolConfig")
    public JedisPoolConfig initJedisConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMinIdle(minIdle);
        jedisPoolConfig.setMaxTotal(maxActive);
        jedisPoolConfig.setMaxWaitMillis(maxWait);
        return jedisPoolConfig;
    }

    @Bean(name = "jedisPool")
    @Deprecated
    public JedisPool getJedispool(@Qualifier("jedisPoolConfig") JedisPoolConfig jedisPoolConfig) {
        return new JedisPool(jedisPoolConfig,host,port,timeout,password);
    }

    @Bean(name="jedisSentinelPool")
    public JedisSentinelPool getJedisSentinelPoll(@Qualifier("jedisPoolConfig") JedisPoolConfig jedisPoolConfig) {
        String[] sentinelsArr = StringUtils.split(sentinelNodes,",");
        Set<String> sentinels = new HashSet<>(sentinelsArr.length);
        sentinels.addAll(Arrays.asList(sentinelsArr));
        return new JedisSentinelPool(clusterName, sentinels, jedisPoolConfig, timeout, password);
    }

}