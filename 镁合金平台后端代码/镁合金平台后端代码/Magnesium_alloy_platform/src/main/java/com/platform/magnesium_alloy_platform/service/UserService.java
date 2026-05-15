package com.platform.magnesium_alloy_platform.service;

import com.platform.magnesium_alloy_platform.dto.UserDTO;
import com.platform.magnesium_alloy_platform.pojo.User;

import java.util.Map;

public interface UserService {

    // 获取用户信息
    User getUserByUsername(String username);

    // 注册新用户
    void registerUser(User user);

    // 登录并返回 JWT Token
    String login(UserDTO userDTO);

    boolean updateUserInfo(String uid, String username, String userUrl);

    boolean forgetPassword(String username, String email, String newPwd);

    boolean updatePassword(String username, String encryptedOldPwd, String encryptedNewPwd);

    Map<String, Object> getUserInfo(String uid);

}