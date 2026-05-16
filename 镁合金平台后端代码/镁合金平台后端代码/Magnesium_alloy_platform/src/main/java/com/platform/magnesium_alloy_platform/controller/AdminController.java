package com.platform.magnesium_alloy_platform.controller;

import com.platform.magnesium_alloy_platform.pojo.Result;
import com.platform.magnesium_alloy_platform.pojo.User;
import com.platform.magnesium_alloy_platform.service.UserService;
import com.platform.magnesium_alloy_platform.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public Result getAllUsers(@RequestHeader(value = "Authorization", required = false) String token) {
        if (!isAdmin(token)) return Result.error("权限不足");
        List<User> users = userService.getAllUsers();
        return Result.success(users);
    }

    @DeleteMapping("/user/{uid}")
    public Result deleteUser(@PathVariable String uid, @RequestHeader(value = "Authorization", required = false) String token) {
        if (!isAdmin(token)) return Result.error("权限不足");
        // 防止删除自己
        String currentUid = JwtUtil.getUidFromJwt(token.replace("Bearer ", ""));
        if (uid.equals(currentUid)) return Result.error("不能删除当前登录的管理员账号");
        
        if (userService.deleteUser(uid)) {
            return Result.success("用户已成功移除");
        }
        return Result.error("用户移除失败");
    }

    private boolean isAdmin(String token) {
        String uid = JwtUtil.getUidFromJwt(token.replace("Bearer ", ""));
        if (uid == null) return false;
        User user = userService.getUserByUid(uid);
        return user != null && user.getRole() == 1;
    }
}
