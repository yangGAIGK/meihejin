package com.platform.magnesium_alloy_platform.mapper;

import com.github.pagehelper.Page;
import com.platform.magnesium_alloy_platform.dto.HistoryDTO;
import com.platform.magnesium_alloy_platform.pojo.ExcelHistory;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface HistoryMapper {

    // 查询历史记录，根据 uid 过滤
    @Select("SELECT * FROM excel_history WHERE uid = #{uid}")
    Page<ExcelHistory> findHistory(HistoryDTO historyDTO);

    // 插入历史记录
    @Insert("INSERT INTO excel_history (uid, aveLength, lisanValue, hardness, yield_strength, strength_extension, createTime, updateTime) " +
            "VALUES (#{uid}, #{aveLength}, #{lisanValue}, #{hardness}, #{yield_strength}, #{strength_extension}, #{createTime}, #{updateTime})")
    void add(ExcelHistory excelHistory);
}
