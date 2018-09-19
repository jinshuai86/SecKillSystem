package com.jinshuai.seckill.service.impl;

import com.jinshuai.seckill.dao.IUserDao;
import com.jinshuai.seckill.entity.User;
import com.jinshuai.seckill.service.IUserService;
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
public class UserServiceImpl implements IUserService {

    @Autowired
    private IUserDao userDao;

    @Override
    public void insertUser(User user) {
        userDao.insertUser(user);
        log.info("添加[{}]成功",user);
    }

    @Override
    public void updateUser(User user) {
        userDao.updateUser(user);
    }

    @Override
    public List<User> listUsers() {
        return userDao.listUsers();
    }

    @Override
    public User getUserById(int userId) {
        return  userDao.getUserById(userId);
    }

}