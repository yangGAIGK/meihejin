package com.platform.magnesium_alloy_platform.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.platform.magnesium_alloy_platform.dto.BpHistoryDTO;
import com.platform.magnesium_alloy_platform.mapper.BpHistoryMapper;
import com.platform.magnesium_alloy_platform.pojo.BpHistory;
import com.platform.magnesium_alloy_platform.pojo.PageResult;
import com.platform.magnesium_alloy_platform.service.BpHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BpHistoryServiceImpl implements BpHistoryService {

    @Autowired
    private BpHistoryMapper bpHistoryMapper;

    @Override
    public PageResult findHistory(BpHistoryDTO bpHistoryDTO) {
        PageHelper.startPage(bpHistoryDTO.getPage(), bpHistoryDTO.getPageSize());
        Page<BpHistory> page = bpHistoryMapper.findHistory(bpHistoryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void addHistory(BpHistory bpHistory) {
        bpHistory.setCreateTime(LocalDateTime.now());
        bpHistory.setUpdateTime(LocalDateTime.now());
        bpHistoryMapper.add(bpHistory);
    }
}
