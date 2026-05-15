package com.platform.magnesium_alloy_platform.mapper;

import com.platform.magnesium_alloy_platform.pojo.UserLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserLogMapper {
    //插入使用日志
    @Insert("insert into user_log(createtime,description) values(#{createTime},#{description})")
    void insert(UserLog log);
}
