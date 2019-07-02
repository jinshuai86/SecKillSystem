package com.jinshuai.seckill.common.dao;

import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author: JS
 * @date: 2019/7/2
 * @description: 获取slave复制状态
 */
@Repository
public interface CommonDao {

    /**
     * 获取slave落后master的秒数
     * */
    @Select("SHOW SLAVE STATUS")
    List<Map<Object, Object>> getBehindSeconds();

}
