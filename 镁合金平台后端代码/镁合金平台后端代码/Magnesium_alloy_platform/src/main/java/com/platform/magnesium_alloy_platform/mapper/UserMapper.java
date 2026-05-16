package com.platform.magnesium_alloy_platform.mapper;

import com.platform.magnesium_alloy_platform.pojo.User;
import org.apache.ibatis.annotations.*;

import java.util.Map;

@Mapper
public interface UserMapper {

    int updateUserInfo(@Param("uid") String uid, @Param("username") String username, @Param("userUrl") String userUrl);

    int forgetPassword(@Param("username") String username, @Param("email") String email, @Param("newPwd") String newPwd);

    int updatePassword(@Param("username") String username, @Param("encryptedOldPwd") String encryptedOldPwd, @Param("encryptedNewPwd") String encryptedNewPwd);

    Map<String, Object> getUserInfo(@Param("uid") String uid);

    @Select("SELECT * FROM user WHERE username = #{username}")
    User getUserByUsername(String username);

    @Select("SELECT * FROM user WHERE email = #{email}")
    User getUserByEmail(String email);

    @Select("SELECT * FROM user WHERE phone = #{phone}")
    User getUserByPhone(String phone);

    // 注册时含 phone 字段
    @Insert("INSERT INTO user (username, password, email, phone, uid, UserUrl, loginFailCount, role) " +
            "VALUES (#{username}, #{password}, #{email}, #{phone}, #{uid}, #{UserUrl}, 0, 0)")
    void insertUser(User user);

    // 找回密码：通过邮箱+手机号验证
    @Select("SELECT * FROM user WHERE email = #{email} AND phone = #{phone}")
    User getUserByEmailAndPhone(@Param("email") String email, @Param("phone") String phone);

    // 重置密码（通过 uid）
    @Update("UPDATE user SET password = #{newPwd}, loginFailCount = 0, lockUntil = null WHERE uid = #{uid}")
    int resetPassword(@Param("uid") String uid, @Param("newPwd") String newPwd);

    // 更新登录失败次数和锁定时间
    @Update("UPDATE user SET loginFailCount = #{failCount}, lockUntil = #{lockUntil} WHERE username = #{username}")
    int updateLoginFail(@Param("username") String username, @Param("failCount") int failCount, @Param("lockUntil") Long lockUntil);

    // 重置登录失败计数
    @Update("UPDATE user SET loginFailCount = 0, lockUntil = null WHERE username = #{username}")
    int resetLoginFail(@Param("username") String username);

    // 管理员：获取所有用户
    @Select("SELECT * FROM user")
    java.util.List<User> getAllUsers();

    // 管理员：按 UID 获取用户
    @Select("SELECT * FROM user WHERE uid = #{uid}")
    User getUserByUid(@Param("uid") String uid);

    // 管理员：删除用户
    @Delete("DELETE FROM user WHERE uid = #{uid}")
    int deleteUser(@Param("uid") String uid);
}