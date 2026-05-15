package com.platform.magnesium_alloy_platform.service;

import com.platform.magnesium_alloy_platform.dto.BpHistoryDTO;
import com.platform.magnesium_alloy_platform.pojo.BpHistory;
import com.platform.magnesium_alloy_platform.pojo.PageResult;

public interface BpHistoryService {
    PageResult findHistory(BpHistoryDTO bpHistoryDTO);
    void addHistory(BpHistory bpHistory);
}
