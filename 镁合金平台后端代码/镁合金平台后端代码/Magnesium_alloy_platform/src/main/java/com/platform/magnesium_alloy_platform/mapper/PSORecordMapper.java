package com.platform.magnesium_alloy_platform.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PSORecordMapper {
    int deleteByIds(@Param("ids") List<Integer> ids);
}