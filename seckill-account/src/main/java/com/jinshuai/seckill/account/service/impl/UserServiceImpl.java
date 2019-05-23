package com.jinshuai.seckill.account.service.impl;

import com.jinshuai.seckill.account.dao.UserDao;
import com.jinshuai.seckill.account.entity.User;
import com.jinshuai.seckill.account.service.UserService;
import com.jinshuai.seckill.common.annotation.TargetDataSource;
import com.jinshuai.seckill.common.constant.DataSourceConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: JS
 * @date: 2018/9/19
 * @description:
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    @Override
    public void insertUser(User user) {
        userDao.insertUser(user);
        log.info("添加[{}]成功", user);
    }

    @Override
    public void updateUser(User user) {
        userDao.updateUser(user);
    }

    @TargetDataSource(DataSourceConstant.SLAVE)
    @Override
    public List<User> listUsers() {
        return userDao.listUsers();
    }

    @TargetDataSource(DataSourceConstant.SLAVE)
    @Override
    public User getUserById(int userId) {
        return userDao.getUserById(userId);
    }

}