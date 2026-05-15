package com.platform.magnesium_alloy_platform.mapper;

import com.platform.magnesium_alloy_platform.pojo.BPParameter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BPParameterMapper {

    /**
     * 分页查询 BP 参数记录，按 createUser 筛选
     *
     * @param offset 偏移量
     * @param pageSize 每页记录数
     * @param createUser 用户名
     * @return BP 参数列表
     */
    @Select("SELECT * FROM bp_parameter WHERE create_user = #{createUser} LIMIT #{offset}, #{pageSize}")
    List<BPParameter> findBPParametersByUser(@Param("offset") int offset,
                                             @Param("pageSize") int pageSize,
                                             @Param("createUser") String createUser);

    /**
     * 统计 BP 参数记录总数，按 createUser 筛选
     *
     * @param createUser 用户名
     * @return 总记录数
     */
    @Select("SELECT COUNT(*) FROM bp_parameter WHERE create_user = #{createUser}")
    int countBPParametersByUser(@Param("createUser") String createUser);
}
