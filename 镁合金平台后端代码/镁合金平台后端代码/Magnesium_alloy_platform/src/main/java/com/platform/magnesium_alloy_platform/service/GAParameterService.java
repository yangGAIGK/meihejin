package com.platform.magnesium_alloy_platform.service;

import java.util.Map;

public interface GAParameterService {

    /**
     * 获取 GA 参数记录的分页数据
     * @param page 当前页码
     * @param pageSize 每页记录数
     * @param createUser 当前用户 ID
     * @return 包含总数和记录列表的分页数据
     */
    Map<String, Object> getGAParameters(int page, int pageSize, String createUser);
}
