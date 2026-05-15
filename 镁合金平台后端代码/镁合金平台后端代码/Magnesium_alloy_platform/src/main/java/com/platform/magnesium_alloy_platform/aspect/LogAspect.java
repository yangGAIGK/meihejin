package com.platform.magnesium_alloy_platform.aspect;

import com.alibaba.fastjson.JSONObject;
import com.platform.magnesium_alloy_platform.mapper.OperateLogMapper;
import com.platform.magnesium_alloy_platform.pojo.OperateLog;
import com.platform.magnesium_alloy_platform.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j//操作执行日志
@Component//创建一个Bean对象
@Aspect//指定当前类是一个切面类
/*
* 这个LogAspect类的作用是在被@Log注解标记的方法执行前后，记录操作员的ID、操作时间、
* 方法执行的相关信息（包括类名、方法名、参数、返回值和执行耗时），并将这些信息保存到数据库中。
* */
public class LogAspect {
    @Autowired
    private OperateLogMapper operateLogMapper;
    @Autowired
    private HttpServletRequest request;
    @Around("@annotation(com.platform.magnesium_alloy_platform.anno.Log)")
    public Object recordLog(ProceedingJoinPoint joinPoint) throws  Throwable{
        //获取请求头中的jwt令牌并解析，获得当前操作员的ID
        String jwt = request.getHeader("Authorization");
        Claims claims = JwtUtil.parseJWT(jwt);
        Integer operateUser = (Integer) claims.get("id");
        LocalDateTime operateTime = LocalDateTime.now();
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        String methodParams = Arrays.toString(args);
        Long begin = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        Long end = System.currentTimeMillis();
        String returnValue = JSONObject.toJSONString(result);
        Long costTime = end - begin;
        //封装操作员信息
        OperateLog operateLog = new OperateLog(null,operateUser,operateTime,className,methodName,methodParams,returnValue,costTime);
        //插入操作员信息
        operateLogMapper.insert(operateLog);
        //返回操作结果
        return result;
    }
}

