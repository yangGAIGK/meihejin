package com.platform.magnesium_alloy_platform.controller;

import com.platform.magnesium_alloy_platform.dto.UserDTO;
import com.platform.magnesium_alloy_platform.pojo.Result;
import com.platform.magnesium_alloy_platform.pojo.User;
import com.platform.magnesium_alloy_platform.service.UserService;
import com.platform.magnesium_alloy_platform.utils.JwtUtil;
import com.platform.magnesium_alloy_platform.utils.Md5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // ==================== 注册 ====================
    @PostMapping("/registerservlet")
    public Result register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String email    = body.get("email");
        String phone    = body.get("phone");
        String userUrl  = body.get("userUrl");

        // 用户名唯一
        if (userService.getUserByUsername(username) != null) {
            return Result.error("用户名已存在");
        }
        // 邮箱唯一
        if (userService.emailExists(email)) {
            return Result.error("该邮箱已被注册");
        }
        // 手机号唯一
        if (userService.phoneExists(phone)) {
            return Result.error("该手机号已被注册");
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setEmail(email);
        newUser.setPhone(phone);
        newUser.setUserUrl(userUrl != null ? userUrl : "https://tyut123.oss-cn-hangzhou.aliyuncs.com/logo.jpeg");

        userService.registerUser(newUser);
        return Result.success("注册成功");
    }

    // ==================== 登录（带锁定） ====================
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody UserDTO userDTO) {
        Map<String, Object> result = userService.loginWithLock(userDTO);
        HttpStatus status = (Integer) result.get("code") == 1 ? HttpStatus.OK : HttpStatus.OK;
        return new ResponseEntity<>(result, status);
    }

    // ==================== 找回密码 - 验证身份（邮箱+手机号） ====================
    @PostMapping("/verifyUser")
    public Result verifyUser(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String phone = body.get("phone");
        String uid = userService.verifyByEmailAndPhone(email, phone);
        if (uid != null) {
            Map<String, String> data = new HashMap<>();
            data.put("uid", uid);
            return Result.success(data);
        }
        return Result.error("邮箱或手机号不匹配");
    }

    // ==================== 找回密码 - 重置密码 ====================
    @PutMapping("/resetPwd")
    public Result resetPwd(@RequestBody Map<String, String> body) {
        String uid    = body.get("uid");
        String newPwd = body.get("newPwd");
        if (userService.resetPassword(uid, newPwd)) {
            return Result.success("密码重置成功");
        }
        return Result.error("密码重置失败");
    }

    // ==================== 旧接口保留兼容 ====================
    @PutMapping("/forgetPwd")
    public ResponseEntity<Map<String, Object>> forgetPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String email = request.get("email");
        String newPwd = request.get("new_pwd");

        Map<String, Object> result = new HashMap<>();
        String encryptedPwd = Md5Util.getMD5String(newPwd);
        if (userService.forgetPassword(username, email, encryptedPwd)) {
            result.put("code", 1);
            result.put("msg", "更新密码成功");
            result.put("data", null);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            result.put("code", 0);
            result.put("msg", "更新密码失败，用户名或邮箱不正确");
            result.put("data", null);
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateUserInfo(
            @RequestBody Map<String, Object> request,
            @RequestHeader("Authorization") String authorizationHeader) {

        String token = authorizationHeader.replace("Bearer ", "");
        String uid = JwtUtil.getUidFromJwt(token);

        if (uid == null || uid.isEmpty()) {
            Map<String, Object> result1 = new HashMap<>();
            result1.put("code", 0);
            result1.put("msg", "Invalid or expired JWT token.");
            result1.put("data", null);
            return new ResponseEntity<>(result1, HttpStatus.UNAUTHORIZED);
        }

        String username = (String) request.get("username");
        String userUrl = (String) request.get("UserUrl");

        Map<String, Object> result = new HashMap<>();
        if (userService.updateUserInfo(uid, username, userUrl)) {
            result.put("code", 1);
            result.put("msg", "更新基本信息成功");
            result.put("data", null);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            result.put("code", 0);
            result.put("msg", "更新基本信息失败");
            result.put("data", null);
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/updatePwd")
    public ResponseEntity<Map<String, Object>> updatePassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String oldPwd = request.get("old_pwd");
        String newPwd = request.get("new_pwd");

        String encryptedOldPwd = Md5Util.getMD5String(oldPwd);
        String encryptedNewPwd = Md5Util.getMD5String(newPwd);

        Map<String, Object> result = new HashMap<>();
        if (userService.updatePassword(username, encryptedOldPwd, encryptedNewPwd)) {
            result.put("code", 1);
            result.put("msg", "修改密码成功");
            result.put("data", null);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            result.put("code", 0);
            result.put("msg", "修改密码失败，用户名或旧密码不正确");
            result.put("data", null);
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getUserInfo(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        String uid = JwtUtil.getUidFromJwt(token);

        if (uid == null || uid.isEmpty()) {
            Map<String, Object> result1 = new HashMap<>();
            result1.put("code", 0);
            result1.put("msg", "Invalid or expired JWT token.");
            result1.put("data", null);
            return new ResponseEntity<>(result1, HttpStatus.UNAUTHORIZED);
        }

        Map<String, Object> userInfo = userService.getUserInfo(uid);
        if (userInfo != null) {
            return new ResponseEntity<>(userInfo, HttpStatus.OK);
        } else {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 0);
            result.put("msg", "Failed to get user info");
            result.put("data", null);
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}