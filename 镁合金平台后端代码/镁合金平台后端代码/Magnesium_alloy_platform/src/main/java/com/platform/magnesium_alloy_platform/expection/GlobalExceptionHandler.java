package com.platform.magnesium_alloy_platform.expection;

import com.platform.magnesium_alloy_platform.pojo.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice//全局处理器
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)//捕获所有类型的异常
    public Result ex(Exception exception){
        exception.printStackTrace();//于打印异常的堆栈跟踪信息到标准错误流（通常是控制台）
        return Result.error("对不起，操作失败，请联系管理员");
    }
}
