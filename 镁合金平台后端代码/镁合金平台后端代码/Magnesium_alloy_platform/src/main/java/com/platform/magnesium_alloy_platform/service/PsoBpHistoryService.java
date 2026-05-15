package com.platform.magnesium_alloy_platform.service;

import com.platform.magnesium_alloy_platform.dto.PsoHistoryDTO;
import com.platform.magnesium_alloy_platform.pojo.PsoBpHistory;  // 使用 PsoBpHistory
import com.platform.magnesium_alloy_platform.pojo.PageResult;

public interface PsoBpHistoryService {
    void addHistory(PsoBpHistory psoBpHistory);  // 使用 PsoBpHistory
    PageResult findHistory(PsoHistoryDTO psoHistoryDTO);  // 使用 PsoHistoryDTO
}
