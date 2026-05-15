package com.platform.magnesium_alloy_platform.mapper;

import com.github.pagehelper.Page;
import com.platform.magnesium_alloy_platform.dto.BpHistoryDTO;
import com.platform.magnesium_alloy_platform.pojo.BpHistory;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BpHistoryMapper {

    @Select("SELECT * FROM bp_history WHERE uid = #{uid}")
    @Results({
            @Result(property = "yieldStrength", column = "yield_strength")  // 映射数据库列名为Java字段
    })
    Page<BpHistory> findHistory(BpHistoryDTO bpHistoryDTO);

    @Insert("INSERT INTO bp_history (ave_length, lisan_value, hardness, yield_strength, strength_extension, " +
            "input_layer, output_layer, intermediate_layer, options, number_of_cycles, learning_rate, error_target_value, uid, create_time, update_time) " +
            "VALUES (#{aveLength}, #{lisanValue}, #{hardness}, #{yieldStrength}, #{strengthExtension}, " +
            "#{inputLayer}, #{outputLayer}, #{intermediateLayer}, #{options}, #{numberOfCycles}, " +
            "#{learningRate}, #{errorTargetValue}, #{uid}, #{createTime}, #{updateTime})")
    void add(BpHistory bpHistory);
}
