package com.platform.magnesium_alloy_platform.service;

import com.platform.magnesium_alloy_platform.dto.HistoryDTO;
import com.platform.magnesium_alloy_platform.pojo.ExcelHistory;
import com.platform.magnesium_alloy_platform.pojo.PageResult;

public interface HistoryService {
    // 查询历史记录
    PageResult findHistory(HistoryDTO historyDTO);

    // 插入历史数据
    void addHistory(ExcelHistory excelHistory);
}
