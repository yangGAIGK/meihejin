package com.platform.magnesium_alloy_platform.mapper;

import com.platform.magnesium_alloy_platform.pojo.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {
    int updateUserInfo(@Param("uid") String uid, @Param("username") String username, @Param("userUrl") String userUrl);

    int forgetPassword(@Param("username") String username, @Param("email") String email, @Param("newPwd") String newPwd);

    int updatePassword(@Param("username") String username, @Param("encryptedOldPwd") String encryptedOldPwd, @Param("encryptedNewPwd") String encryptedNewPwd);

    Map<String, Object> getUserInfo(@Param("uid") String uid);

    // 根据用户名查询用户
    @Select("SELECT * FROM user WHERE username = #{username}")
    User getUserByUsername(String username);

    // 插入新用户
    @Insert("INSERT INTO user (username, password, email, uid, UserUrl) VALUES (#{username}, #{password}, #{email}, #{uid}, #{UserUrl})")
    void insertUser(User user);
}