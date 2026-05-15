package com.platform.magnesium_alloy_platform.mapper;


import com.platform.magnesium_alloy_platform.pojo.OperateLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperateLogMapper {

    //插入操作日志
    @Insert("insert into operate_log (operateuser, operatetime, classname, methodname, methodparams, returnvalue, costtime) " +
            "values (#{operateUser}, #{operateTime}, #{className}, #{methodName}, #{methodParams}, #{returnValue}, #{costTime});")
    public void insert(OperateLog log);
}
