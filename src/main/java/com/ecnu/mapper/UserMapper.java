package com.ecnu.mapper;

import com.ecnu.pojo.User;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

// 这个注解表示该类是一个mybatis的mapper类
@Mapper
@Repository
public interface UserMapper {

    // 查询所有用户
    @Select("select * from user")
    List<User> selectAllUser();

    // 根据用户名查询用户
    @Select("select * from user where username=#{username}")
    User selectUserByUsername(String username);

    // 根据用户名查询用户个数
    @Select("select count(*) from user where username=#{username}")
    int countUserNum(String username);

    // 添加用户
    @Insert("insert into user(username, password, role) values(#{username}, #{password}, #{role})")
    void addUser(User user);

    // 更新用户
    @Update("update user set password=#{password}, role=#{role} where username=#{username}")
    void updateUser(User user);

    // 删除用户
    @Delete("delete from user where username=#{username}")
    void deleteUser(String username);

}
