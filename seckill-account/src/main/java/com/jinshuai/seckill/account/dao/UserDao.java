package com.jinshuai.seckill.account.dao;

import com.jinshuai.seckill.account.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDao {

    @Insert("INSERT INTO user (username,phone) VALUES(#{username},#{phone})")
    void insertUser(User user);

    void updateUser(User user);

    @Select("SELECT * FROM user")
    List<User> listUsers();

    @Select("SELECT * FROM user WHERE id = #{id}")
    User getUserById(long userId);

}