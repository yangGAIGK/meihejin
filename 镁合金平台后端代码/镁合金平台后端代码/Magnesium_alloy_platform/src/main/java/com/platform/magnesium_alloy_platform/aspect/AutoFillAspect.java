package com.platform.magnesium_alloy_platform.aspect;


import com.platform.magnesium_alloy_platform.anno.AutoFill;
import com.platform.magnesium_alloy_platform.constant.AutoFillConstant;
import com.platform.magnesium_alloy_platform.enumeration.OperationType;
import com.platform.magnesium_alloy_platform.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Map;

@Aspect //定义这是一个切面类
@Component//将当前类的实现对象当作一个bean管理
@Slf4j//日志输出
//切面类，主要功能是在执行数据库操作前自动填充实体类的公共字段。比如创建时间、创建用户、更新时间和更新用户等字段，以简化业务逻辑代码
public class AutoFillAspect {
    /*
      切入点：拦截哪些类的哪些方法
      切入点表达式：对于哪些类和哪些方法实施拦截
     */
    @Pointcut("execution(* com.platform.magnesium_alloy_platform.mapper.*.*(..)) && @annotation(com.platform.magnesium_alloy_platform.anno.AutoFill)")
    public void autoFillPointCut() {
    }

    /*
     * 前置通知，在sql语句执行前，在通知中进行公共字段的赋值
     * @Before 里面存放的是切入点表达式
     * */
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始进行公共字段的填充。。。");
        /*
         * 获取到当前被拦截的方法上的数据库操作类型
         * 方法签名是指一个方法的唯一标识。它由方法的名称、参数类型以及返回类型组成
         * 方法签名的目的是区分不同的方法，确保编译器和虚拟机能正确的调用对应的方法
         * */
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//获得方法签名对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获取方法上的注解对象
        OperationType operationType = autoFill.value(); //获取数据库操作类型

        //获取当前被拦截的方法的参数--实体对象4
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }
        Object entity = args[0];//获取实体对象
        LocalDateTime now = LocalDateTime.now();//获取当前时间
        Map<String,Object> cliams = ThreadLocalUtil.get();//从当前线程获取用户ID
        Integer currentId = (Integer)  cliams.get("id");
        //处理插入操作
        if (operationType == OperationType.INSERT){
            try {//使用反射机制，获得有参的setCreateTime方法，获得方法名和方法参数
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setCreateTime.invoke(entity,now);//通过方法名和方法参数通过反射获取带形参的方法,传入上一步获取的方法的实例对象以及方法传入的实参，注入对象方法和实参
                setCreateUser.invoke(entity,currentId);
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        //如果操作类型是更新（UPDATE），则执行以下代码块，与插入操作类似，但只填充更新时间和更新用户字段。
        }else if(operationType == OperationType.UPDATE){
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);

                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
