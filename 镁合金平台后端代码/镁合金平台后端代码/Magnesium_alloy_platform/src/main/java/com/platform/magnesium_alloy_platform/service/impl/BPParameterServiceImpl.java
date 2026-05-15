package com.platform.magnesium_alloy_platform.service.impl;

import com.platform.magnesium_alloy_platform.mapper.BPParameterMapper;
import com.platform.magnesium_alloy_platform.pojo.BPParameter;
import com.platform.magnesium_alloy_platform.service.BPParameterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BPParameterServiceImpl implements BPParameterService {

    @Autowired
    private BPParameterMapper bpParameterMapper;

    /**
     * 获取分页形式的 BP 参数记录，按 createUser 字段筛选
     *
     * @param page 当前页码
     * @param pageSize 每页记录数
     * @param createUser 当前用户
     * @return 包含总数和记录列表的分页数据
     */
    @Override
    public Map<String, Object> getBPParameters(int page, int pageSize, String createUser) {
        // 计算分页偏移量
        int offset = (page - 1) * pageSize;

        // 查询分页数据，按照 createUser 筛选
        List<BPParameter> parameters = bpParameterMapper.findBPParametersByUser(offset, pageSize, createUser);

        // 查询总记录数，按照 createUser 筛选
        int total = bpParameterMapper.countBPParametersByUser(createUser);

        // 封装返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("total", total); // 总记录数
        result.put("rows", parameters); // 当前页的数据列表
        return result;
    }
}
