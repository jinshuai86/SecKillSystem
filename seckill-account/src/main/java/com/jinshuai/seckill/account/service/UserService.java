package com.jinshuai.seckill.account.service;

import com.jinshuai.seckill.account.entity.User;

import java.util.List;

public interface UserService {

    void insertUser(User user);

    void updateUser(User user);

    List<User> listUsers();

    User getUserById(long userId);

}