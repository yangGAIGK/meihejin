package com.platform.magnesium_alloy_platform.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
// 应用Lombok的Data注释,为类生成各种遍历方法
@NoArgsConstructor
//为类生成一个无参构造函数
@AllArgsConstructor
//为类生成一个包含所有字段的构造函数
public class UserLog {

    private Integer id;
    @JsonFormat(pattern =  "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private String description;

    @Override
    public String toString() {
        return "UserLog{" +
                "id=" + id +
                ", createTime=" + createTime +
                ", description='" + description + '\'' +
                '}';
    }
}
