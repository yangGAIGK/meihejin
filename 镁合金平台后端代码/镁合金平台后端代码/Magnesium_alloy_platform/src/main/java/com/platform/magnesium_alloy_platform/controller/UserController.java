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

    // 注册接口
    @PostMapping("/registerservlet")  // 这个路径会是 "/user/registerservlet"
    public Result register(@RequestBody UserDTO userDTO) {
        // 检查用户名是否已存在
        User existingUser = userService.getUserByUsername(userDTO.getUsername());
        if (existingUser != null) {
            return Result.error("Username already exists.");
        }

        // 创建新用户并注册
        User newUser = new User();
        newUser.setUsername(userDTO.getUsername());
        newUser.setPassword(userDTO.getPassword());
        newUser.setEmail(userDTO.getEmail());
        newUser.setUserUrl(userDTO.getUserUrl());

        // 注册用户
        userService.registerUser(newUser);

        return Result.success("Registration successful.");
    }

    // 登录接口
    @PostMapping("/login")  // 这个路径会是 "/user/login"
    public Result login(@RequestBody UserDTO userDTO) {
        // 登录验证
        String token = userService.login(userDTO);

        if (token == null) {
            return Result.error("Invalid username or password.");
        }

        // 登录成功，返回 JWT
        return Result.success(token);
    }

    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateUserInfo(
            @RequestBody Map<String, Object> request,
            @RequestHeader("Authorization") String authorizationHeader) {

        // 从 Authorization 头中提取 JWT token（假设格式为 "Bearer <token>"）
        String token = authorizationHeader.replace("Bearer ", "");

        // 获取当前用户的 UID，从 JWT token 中提取
        String uid = JwtUtil.getUidFromJwt(token);

        // 检查 uid 是否为 null 或空
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
        // 从 Authorization 头中提取 JWT token（假设格式为 "Bearer <token>"）
        String token = authorizationHeader.replace("Bearer ", "");

        // 获取当前用户的 UID，从 JWT token 中提取
        String uid = JwtUtil.getUidFromJwt(token);

        // 检查 uid 是否为 null 或空
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