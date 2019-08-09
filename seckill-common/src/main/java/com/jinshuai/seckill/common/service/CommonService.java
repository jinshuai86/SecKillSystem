package com.jinshuai.seckill.common.service;

import java.util.List;
import java.util.Map;

/**
 * @author: JS
 * @date: 2019/7/2
 * @description:
 */
public interface CommonService {
    /**
     * 获取slave复制状态
     */
    List<Map<Object, Object>> getBehindSeconds();

}
