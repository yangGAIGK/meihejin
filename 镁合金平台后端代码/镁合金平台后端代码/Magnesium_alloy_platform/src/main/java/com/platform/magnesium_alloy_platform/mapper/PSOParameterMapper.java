package com.platform.magnesium_alloy_platform.mapper;

import com.platform.magnesium_alloy_platform.pojo.PSOParameter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PSOParameterMapper {

    /**
     * 分页查询 PSO 参数记录，按 createUser 筛选
     *
     * @param offset 偏移量
     * @param pageSize 每页记录数
     * @param createUser 用户名
     * @return PSO 参数列表
     */
    @Select("SELECT * FROM pso_parameter WHERE create_user = #{createUser} LIMIT #{offset}, #{pageSize}")
    List<PSOParameter> findPSOParametersByUser(@Param("offset") int offset,
                                               @Param("pageSize") int pageSize,
                                               @Param("createUser") String createUser);

    /**
     * 统计 PSO 参数记录总数，按 createUser 筛选
     *
     * @param createUser 用户名
     * @return 总记录数
     */
    @Select("SELECT COUNT(*) FROM pso_parameter WHERE create_user = #{createUser}")
    int countPSOParametersByUser(@Param("createUser") String createUser);
}
