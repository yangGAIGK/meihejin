package com.platform.magnesium_alloy_platform.service.impl;

import com.platform.magnesium_alloy_platform.mapper.PSO_HistoryMapper;
import com.platform.magnesium_alloy_platform.service.PSO_HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class PSO_HistoryServiceImpl implements PSO_HistoryService {

    @Autowired
    private PSO_HistoryMapper historyPSO;

    @Override
    public boolean deleteByIds(List<Integer> ids) {
        return historyPSO.deleteByIds(ids) > 0;
    }

    @Override
    public List<Map<String, Object>> filterPSOHistory(
            String uid,
            Double minAveLength,
            Double maxAveLength,
            Double minLisanValue,
            Double maxLisanValue) {
        return historyPSO.filterPSOHistory(uid, minAveLength, maxAveLength, minLisanValue, maxLisanValue);
    }

}