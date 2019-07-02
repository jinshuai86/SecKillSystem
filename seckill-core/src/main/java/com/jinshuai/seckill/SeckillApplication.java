package com.jinshuai.seckill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@MapperScan(basePackages = {"com.jinshuai.seckill.dao", "com.jinshuai.seckill.account.dao", "com.jinshuai.seckill.product.dao", "com.jinshuai.seckill.order.dao", "com.jinshuai.seckill.common.dao"})
public class SeckillApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeckillApplication.class, args);
    }
}
