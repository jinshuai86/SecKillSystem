package com.jinshuai.seckill.product.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: JS
 * @date: 2018/6/3
 * @description:
 */
@Data
public class Product {

    /**
     * 产品编号
     */
    private int id;

    /**
     * 产品名
     */
    private String productName;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 版本号
     */
    private Integer version;

}