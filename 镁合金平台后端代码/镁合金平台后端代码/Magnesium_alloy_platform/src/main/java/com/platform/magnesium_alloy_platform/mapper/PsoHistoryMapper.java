package com.platform.magnesium_alloy_platform.mapper;

import com.github.pagehelper.Page;
import com.platform.magnesium_alloy_platform.dto.PsoHistoryDTO;
import com.platform.magnesium_alloy_platform.pojo.PsoHistory;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PsoHistoryMapper {

    // 查询历史记录
    @Select("SELECT * FROM pso_history WHERE uid = #{uid}")
    @Results({
            @Result(property = "yieldStrength", column = "yield_strength"),
            @Result(property = "strengthExtension", column = "strength_extension"),
            @Result(property = "maxNum", column = "max_num"),
            @Result(property = "topValue", column = "top_value"),
            @Result(property = "lowValue", column = "low_value"),
            @Result(property = "swarmSize", column = "swarm_size"),
            @Result(property = "individualFactor", column = "individual_factor"),
            @Result(property = "groupFactor", column = "group_factor"),
            @Result(property = "inertiaFactor", column = "inertia_factor"),
            @Result(property = "createTime", column = "createTime"),
            @Result(property = "updateTime", column = "updateTime")
    })
    Page<PsoHistory> findHistory(PsoHistoryDTO psoHistoryDTO);

    // 插入数据
    @Insert("INSERT INTO pso_history (aveLength, lisanValue, hardness, yield_strength, strength_extension, " +
            "max_num, top_value, low_value, swarm_size, individual_factor, group_factor, inertia_factor, uid, createTime, updateTime) " +
            "VALUES (#{aveLength}, #{lisanValue}, #{hardness}, #{yieldStrength}, #{strengthExtension}, " +
            "#{maxNum}, #{topValue}, #{lowValue}, #{swarmSize}, #{individualFactor}, #{groupFactor}, #{inertiaFactor}, #{uid}, #{createTime}, #{updateTime})")
    void add(PsoHistory psoHistory);
}
