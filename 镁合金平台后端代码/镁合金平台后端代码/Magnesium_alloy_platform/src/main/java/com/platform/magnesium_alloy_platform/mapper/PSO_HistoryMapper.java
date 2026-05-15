package com.platform.magnesium_alloy_platform.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface PSO_HistoryMapper {
    int deleteByIds(@Param("ids") List<Integer> ids);

    List<Map<String, Object>> filterPSOHistory(
            @Param("uid") String uid,
            @Param("minAveLength") Double minAveLength,
            @Param("maxAveLength") Double maxAveLength,
            @Param("minLisanValue") Double minLisanValue,
            @Param("maxLisanValue") Double maxLisanValue);
}