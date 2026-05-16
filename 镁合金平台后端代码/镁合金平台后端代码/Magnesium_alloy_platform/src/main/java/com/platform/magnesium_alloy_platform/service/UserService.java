package com.platform.magnesium_alloy_platform.service;

import com.platform.magnesium_alloy_platform.dto.UserDTO;
import com.platform.magnesium_alloy_platform.pojo.User;

import java.util.Map;

public interface UserService {

    User getUserByUsername(String username);

    /** 检查邮箱是否已被注册 */
    boolean emailExists(String email);

    /** 检查手机号是否已被注册 */
    boolean phoneExists(String phone);

    /** 注册（含 phone 字段） */
    void registerUser(User user);

    /**
     * 登录，带失败次数锁定逻辑
     * @return token（成功）或 null（失败），失败时含错误信息在 result 中
     */
    Map<String, Object> loginWithLock(UserDTO userDTO);

    boolean updateUserInfo(String uid, String username, String userUrl);

    /** 通过邮箱+手机号验证身份，返回 uid（成功）或 null */
    String verifyByEmailAndPhone(String email, String phone);

    /** 重置密码（通过 uid） */
    boolean resetPassword(String uid, String newPwd);

    boolean forgetPassword(String username, String email, String newPwd);

    boolean updatePassword(String username, String encryptedOldPwd, String encryptedNewPwd);

    Map<String, Object> getUserInfo(String uid);
}