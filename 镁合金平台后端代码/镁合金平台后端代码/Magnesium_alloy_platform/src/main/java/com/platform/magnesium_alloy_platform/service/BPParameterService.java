package com.platform.magnesium_alloy_platform.service;

import java.util.Map;

public interface BPParameterService {

    /**
     * 获取分页形式的 BP 参数记录，按 createUser 字段筛选
     *
     * @param page 当前页码
     * @param pageSize 每页记录数
     * @param createUser 当前用户
     * @return 包含总数和记录列表的分页数据
     */
    Map<String, Object> getBPParameters(int page, int pageSize, String createUser);
}
