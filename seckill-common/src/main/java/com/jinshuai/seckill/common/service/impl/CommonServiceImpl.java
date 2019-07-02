package com.jinshuai.seckill.common.service.impl;

import com.jinshuai.seckill.common.annotation.TargetDataSource;
import com.jinshuai.seckill.common.constant.DataSourceConstant;
import com.jinshuai.seckill.common.dao.CommonDao;
import com.jinshuai.seckill.common.service.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author: JS
 * @date: 2019/7/2
 * @description:
 */
@Service
public class CommonServiceImpl implements CommonService {

    @Autowired
    private CommonDao commonDao;

    @TargetDataSource(value = DataSourceConstant.SLAVE)
    @Override
    public List<Map<Object, Object>> getBehindSeconds() {
        return commonDao.getBehindSeconds();
    }
}
