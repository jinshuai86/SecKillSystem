package com.jinshuai.seckill.service;

import java.util.List;
import com.jinshuai.seckill.entity.User;

public interface IUserService {

    void insertUser(User user);

    void updateUser(User user);

    List<User> listUsers();

    User getUserById(int userId);

}