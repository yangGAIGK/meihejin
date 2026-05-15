package com.platform.magnesium_alloy_platform.mapper;

import com.platform.magnesium_alloy_platform.pojo.GAParameter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GAParameterMapper {

    /**
     * 分页查询 GA 参数记录
     *
     * @param offset 偏移量
     * @param pageSize 每页记录数
     * @param createUser 当前用户 ID
     * @return GA 参数列表
     */
    @Select("SELECT * FROM ga_parameter WHERE create_user = #{createUser} LIMIT #{offset}, #{pageSize}")
    List<GAParameter> findGAParameters(@Param("offset") int offset, @Param("pageSize") int pageSize, @Param("createUser") String createUser);

    /**
     * 统计 GA 参数记录总数
     *
     * @param createUser 当前用户 ID
     * @return 总记录数
     */
    @Select("SELECT COUNT(*) FROM ga_parameter WHERE create_user = #{createUser}")
    int countGAParameters(@Param("createUser") String createUser);
}
