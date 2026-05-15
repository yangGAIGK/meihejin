package com.platform.magnesium_alloy_platform.mapper;

import com.platform.magnesium_alloy_platform.pojo.TempPredictHistory;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.jdbc.SQL;

import java.util.List;
import java.util.Map;

@Mapper
public interface TempPredictHistoryMapper {

    @Insert("INSERT INTO temp_history(file_name, temp, time) VALUES (#{fileName}, #{temp}, #{time})")
    void insert(TempPredictHistory history);

    @Select("SELECT id, file_name AS fileName, temp, time FROM temp_history ORDER BY time DESC")
    List<TempPredictHistory> listAll();

    @Delete("DELETE FROM temp_history WHERE id = #{id}")
    void deleteById(Integer id);

    @DeleteProvider(type = TempPredictHistoryMapper.SqlProvider.class, method = "batchDeleteSql")
    void deleteByIds(@Param("ids") List<Integer> ids);

    class SqlProvider {
        public String batchDeleteSql(Map<String, Object> params) {
            @SuppressWarnings("unchecked")
            List<Integer> ids = (List<Integer>) params.get("ids");
            StringBuilder sb = new StringBuilder("DELETE FROM temp_history WHERE id IN (");
            for (int i = 0; i < ids.size(); i++) {
                sb.append("#{ids[").append(i).append("]}");
                if (i < ids.size() - 1) sb.append(",");
            }
            sb.append(")");
            return sb.toString();
        }
    }
}
