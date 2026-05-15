package com.platform.magnesium_alloy_platform.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer id;  // 用户在数据库中的 ID
    private String username;  // 用户名
    private String password;  // 密码
    private String uid;  // 唯一用户标识符 (已更改为 uid)
    private String email;  // 邮箱
    private String UserUrl; //url
}
