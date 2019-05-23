package com.jinshuai.seckill.dao;

import com.jinshuai.seckill.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IUserDao {

    @Insert("INSERT INTO user (username,phone) VALUES(#{username},#{phone})")
    void insertUser(User user);

    void updateUser(User user);

    @Select("SELECT * FROM user")
    List<User> listUsers();

    User getUserById(int userId);

}