package com.platform.magnesium_alloy_platform.service.impl;

import com.platform.magnesium_alloy_platform.dto.UserDTO;
import com.platform.magnesium_alloy_platform.mapper.UserMapper;
import com.platform.magnesium_alloy_platform.pojo.User;
import com.platform.magnesium_alloy_platform.service.UserService;
import com.platform.magnesium_alloy_platform.utils.JwtUtil;
import com.platform.magnesium_alloy_platform.utils.Md5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userDao;

    @Override
    public User getUserByUsername(String username) {
        return userDao.getUserByUsername(username);
    }

    @Override
    public void registerUser(User user) {
        // 生成唯一的 uid
        String uid = UUID.randomUUID().toString();
        user.setUid(uid);  // 设置 uid

        // 密码加密
        String encryptedPassword = Md5Util.getMD5String(user.getPassword());
        user.setPassword(encryptedPassword);

        // 将用户信息插入数据库
        userDao.insertUser(user);
    }

    @Override
    public String login(UserDTO userDTO) {
        // 根据用户名查询用户
        User user = userDao.getUserByUsername(userDTO.getUsername());
        if (user != null && Md5Util.getMD5String(userDTO.getPassword()).equals(user.getPassword())) {
            // 登录成功，生成并返回 JWT token
            return JwtUtil.generateJwt(user.getUid());  // 使用 uid 生成 JWT
        }
        return null;  // 登录失败
    }

    @Override
    public boolean updateUserInfo(String uid, String username, String userUrl) {
        return userDao.updateUserInfo(uid, username, userUrl) > 0;
    }

    @Override
    public boolean forgetPassword(String username, String email, String newPwd) {
        return userDao.forgetPassword(username, email, newPwd) > 0;
    }

    @Override
    public boolean updatePassword(String username, String encryptedOldPwd, String encryptedNewPwd) {
        return userDao.updatePassword(username, encryptedOldPwd, encryptedNewPwd) > 0;
    }

    @Override
    public Map<String, Object> getUserInfo(String uid) {
        return userDao.getUserInfo(uid);
    }
}