package com.jinshuai.seckill.mq;

/**
 * 生产者接口
 */
public interface Producer<T> {

    /**
     * 创建队列元素
     */
    void product(T t);

}
