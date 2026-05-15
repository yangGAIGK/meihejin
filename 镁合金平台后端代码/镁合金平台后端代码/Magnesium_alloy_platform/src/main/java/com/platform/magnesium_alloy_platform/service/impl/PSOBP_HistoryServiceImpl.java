package com.platform.magnesium_alloy_platform.service.impl;

import com.platform.magnesium_alloy_platform.mapper.PSOBP_HistoryMapper;
import com.platform.magnesium_alloy_platform.service.PSOBP_HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PSOBP_HistoryServiceImpl implements PSOBP_HistoryService {

    @Autowired
    private PSOBP_HistoryMapper psobpHistoryMapper;

    @Override
    public boolean deleteByIds(List<Integer> ids) {
        return psobpHistoryMapper.deleteByIds(ids) > 0;
    }
}
