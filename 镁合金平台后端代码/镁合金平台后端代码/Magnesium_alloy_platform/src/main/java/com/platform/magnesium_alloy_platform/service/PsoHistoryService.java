package com.platform.magnesium_alloy_platform.service;

import com.platform.magnesium_alloy_platform.dto.PsoHistoryDTO;
import com.platform.magnesium_alloy_platform.pojo.PsoHistory;
import com.platform.magnesium_alloy_platform.pojo.PageResult;

public interface PsoHistoryService {
    void addHistory(PsoHistory psoHistory);
    PageResult findHistory(PsoHistoryDTO psoHistoryDTO);  // 新增查询历史记录的方法
}
