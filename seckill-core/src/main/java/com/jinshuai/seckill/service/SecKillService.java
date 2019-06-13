package com.jinshuai.seckill.service;

import com.jinshuai.seckill.common.enums.StatusEnum;
import com.jinshuai.seckill.common.exception.SecKillException;

import java.util.Map;

public interface SecKillService {

    /**
     * 乐观锁
     */
    StatusEnum updateStockByOptimisticLock(Map<String, Long> parameter) throws SecKillException;

    /**
     * 悲观锁
     */
    StatusEnum updateStockByPessimisticLock(Map<String, Long> parameter);

}