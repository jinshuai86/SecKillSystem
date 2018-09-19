package com.jinshuai.seckill.controller;

import com.jinshuai.seckill.annotation.TargetDataSource;
import com.jinshuai.seckill.constant.DataSourceConstant;
import com.jinshuai.seckill.entity.User;
import com.jinshuai.seckill.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author: JS
 * @date: 2018/9/19
 * @description: MySQL主从分离
 */
@RestController
public class UserController {

    @Autowired
    private IUserService userService;

    @GetMapping("/users")
    @TargetDataSource(DataSourceConstant.SLAVE)
    public List<User> listUsers() {
        List<User> result = userService.listUsers();
        return result;
    }

    @PostMapping("/user")
    @TargetDataSource
    public void insertUser(@RequestBody User user) {
        userService.insertUser(user);
    }

    @PutMapping("/user")
    @TargetDataSource
    public void updateUser(@PathVariable int userId) {
        User user = userService.getUserById(userId);
        user.setUsername("靳帅_测试");
        userService.updateUser(user);
    }

}