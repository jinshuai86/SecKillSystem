package com.jinshuai.seckill.mq;

/**
 * 消费者统一接口
 */
public interface Consumer<T> {

    /**
     * 消费队列元素
     */
    void consume(T t);

}
