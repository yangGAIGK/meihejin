package com.platform.magnesium_alloy_platform.service.impl;

import com.platform.magnesium_alloy_platform.mapper.GAParameterMapper;
import com.platform.magnesium_alloy_platform.pojo.GAParameter;
import com.platform.magnesium_alloy_platform.service.GAParameterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GAParameterServiceImpl implements GAParameterService {

    @Autowired
    private GAParameterMapper gaParameterMapper;

    @Override
    public Map<String, Object> getGAParameters(int page, int pageSize, String createUser) {
        // 计算分页偏移量
        int offset = (page - 1) * pageSize;

        // 查询该用户的 GA 参数记录
        List<GAParameter> parameters = gaParameterMapper.findGAParameters(offset, pageSize, createUser);

        // 查询总记录数
        Integer total = gaParameterMapper.countGAParameters(createUser);  // countGAParameters 返回 Integer 类型

        // 封装返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("total", total.longValue()); // 确保 total 是 Long 类型
        result.put("rows", parameters); // 当前页的数据列表
        return result;
    }
}
