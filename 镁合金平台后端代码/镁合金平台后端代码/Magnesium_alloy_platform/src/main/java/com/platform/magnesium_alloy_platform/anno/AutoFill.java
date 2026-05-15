package com.platform.magnesium_alloy_platform.anno;

import com.platform.magnesium_alloy_platform.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//自定义注解，用于标识某个方法需要进行功能字段填充处理
@Target(ElementType.METHOD)//当前注解只能加在方法上
@Retention(RetentionPolicy.RUNTIME)//该注解在编译后会被保留到字节码文件中，并且在运行时可以通过反射机制加载到JVM中，以便在程序运行时使用
public @interface AutoFill {
    OperationType value();
    //数据库的操作类型，update、insert valer= OperationType.UPDATE
}

