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
public class OperateLog {

    private Integer id; //主键ID
    private Integer operateUser; //操作人ID
    @JsonFormat(pattern =  "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operateTime; //操作时间
    private String className; //操作类名
    private String methodName; //操作方法名
    private String methodParams; //操作方法参数
    private String returnValue; //操作方法返回值
    private Long costTime; //操作耗时
}
