package com.platform.magnesium_alloy_platform.service.impl;

import com.platform.magnesium_alloy_platform.dto.UserDTO;
import com.platform.magnesium_alloy_platform.mapper.UserMapper;
import com.platform.magnesium_alloy_platform.pojo.User;
import com.platform.magnesium_alloy_platform.service.UserService;
import com.platform.magnesium_alloy_platform.utils.JwtUtil;
import com.platform.magnesium_alloy_platform.utils.Md5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private static final int MAX_FAIL = 5;
    private static final long LOCK_DURATION_MS = 5 * 60 * 1000L; // 5分钟

    @Autowired
    private UserMapper userDao;

    @Override
    public User getUserByUsername(String username) {
        return userDao.getUserByUsername(username);
    }

    @Override
    public boolean emailExists(String email) {
        return userDao.getUserByEmail(email) != null;
    }

    @Override
    public boolean phoneExists(String phone) {
        return userDao.getUserByPhone(phone) != null;
    }

    @Override
    public void registerUser(User user) {
        user.setUid(UUID.randomUUID().toString());
        user.setPassword(Md5Util.getMD5String(user.getPassword()));
        userDao.insertUser(user);
    }

    @Override
    public Map<String, Object> loginWithLock(UserDTO userDTO) {
        Map<String, Object> result = new HashMap<>();
        User user = userDao.getUserByUsername(userDTO.getUsername());

        if (user == null) {
            result.put("code", 0);
            result.put("msg", "用户名或密码错误");
            return result;
        }

        // 检查是否被锁定
        Long lockUntil = user.getLockUntil();
        if (lockUntil != null && System.currentTimeMillis() < lockUntil) {
            long remaining = (lockUntil - System.currentTimeMillis()) / 1000;
            result.put("code", -1);
            result.put("msg", "账号已被锁定，请 " + remaining + " 秒后重试");
            return result;
        }

        // 密码校验
        if (!Md5Util.getMD5String(userDTO.getPassword()).equals(user.getPassword())) {
            int failCount = (user.getLoginFailCount() == null ? 0 : user.getLoginFailCount()) + 1;
            Long newLockUntil = null;
            if (failCount >= MAX_FAIL) {
                newLockUntil = System.currentTimeMillis() + LOCK_DURATION_MS;
            }
            userDao.updateLoginFail(user.getUsername(), failCount, newLockUntil);

            if (failCount >= MAX_FAIL) {
                result.put("code", -1);
                result.put("msg", "密码连续错误 " + MAX_FAIL + " 次，账号锁定 5 分钟");
            } else {
                result.put("code", 0);
                result.put("msg", "用户名或密码错误，还剩 " + (MAX_FAIL - failCount) + " 次尝试机会");
            }
            return result;
        }

        // 登录成功，重置失败计数
        userDao.resetLoginFail(user.getUsername());
        String token = JwtUtil.generateJwt(user.getUid());
        result.put("code", 1);
        result.put("data", token);
        result.put("msg", "登录成功");
        return result;
    }

    @Override
    public boolean updateUserInfo(String uid, String username, String userUrl) {
        return userDao.updateUserInfo(uid, username, userUrl) > 0;
    }

    @Override
    public String verifyByEmailAndPhone(String email, String phone) {
        User user = userDao.getUserByEmailAndPhone(email, phone);
        return user != null ? user.getUid() : null;
    }

    @Override
    public boolean resetPassword(String uid, String newPwd) {
        return userDao.resetPassword(uid, Md5Util.getMD5String(newPwd)) > 0;
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

    @Override
    public java.util.List<User> getAllUsers() {
        return userDao.getAllUsers();
    }

    @Override
    public User getUserByUid(String uid) {
        return userDao.getUserByUid(uid);
    }

    @Override
    public boolean deleteUser(String uid) {
        return userDao.deleteUser(uid) > 0;
    }
}