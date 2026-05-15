package com.platform.magnesium_alloy_platform.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
// 应用Lombok的Data注释,为类生成各种遍历方法
@NoArgsConstructor
//为类生成一个无参构造函数
@AllArgsConstructor
//为类生成一个包含所有字段的构造函数
public class ResultT<T> implements Serializable {

    private Integer code; //编码：1成功，0和其它数字为失败
    private String msg; //错误信息
    private T data; //数据

    public static <T> ResultT<T> success() {
        ResultT<T> result = new ResultT<T>();
        result.code = 1;
        return result;
    }

    public static <T> ResultT<T> success(T object) {
        ResultT<T> result = new ResultT<T>();
        result.data = object;
        result.code = 1;
        return result;
    }

    public static <T> ResultT<T> error(String msg) {
        ResultT result = new ResultT();
        result.msg = msg;
        result.code = 0;
        return result;
    }
}
