package com.platform.magnesium_alloy_platform.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer id;        // 用户在数据库中的 ID
    private String username;   // 用户名
    private String password;   // 密码（MD5）
    private String uid;        // 唯一用户标识符
    private String email;      // 邮箱（唯一）
    private String phone;      // 手机号（唯一）
    private String UserUrl;    // 头像url
    private Integer loginFailCount;  // 连续登录失败次数
    private Long lockUntil;          // 账号锁定截止时间戳（毫秒），null表示未锁
    private Integer role;      // 角色：0=普通用户 1=管理员
}
