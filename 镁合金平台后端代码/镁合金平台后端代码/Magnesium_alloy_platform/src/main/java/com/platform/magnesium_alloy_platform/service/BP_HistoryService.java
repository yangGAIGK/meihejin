package com.platform.magnesium_alloy_platform.service;

import java.util.List;
import java.util.Map;

public interface BP_HistoryService {

    boolean deleteByIds(List<Integer> ids);

    List<Map<String, Object>> filterBPHistory(
            String uid,
            Double minAveLength,
            Double maxAveLength,
            Double minLisanValue,
            Double maxLisanValue);
}
