package com.platform.magnesium_alloy_platform.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.platform.magnesium_alloy_platform.dto.PsoHistoryDTO;
import com.platform.magnesium_alloy_platform.mapper.PsoHistoryMapper;
import com.platform.magnesium_alloy_platform.pojo.PsoHistory;
import com.platform.magnesium_alloy_platform.pojo.PageResult;
import com.platform.magnesium_alloy_platform.service.PsoHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PsoHistoryServiceImpl implements PsoHistoryService {

    @Autowired
    private PsoHistoryMapper psoHistoryMapper;

    @Override
    public void addHistory(PsoHistory psoHistory) {
        psoHistory.setCreateTime(LocalDateTime.now());
        psoHistory.setUpdateTime(LocalDateTime.now());
        psoHistoryMapper.add(psoHistory);
    }

    @Override
    public PageResult findHistory(PsoHistoryDTO psoHistoryDTO) {
        PageHelper.startPage(psoHistoryDTO.getPage(), psoHistoryDTO.getPageSize());
        Page<PsoHistory> page = psoHistoryMapper.findHistory(psoHistoryDTO);  // 假设您已实现findHistory方法
        return new PageResult(page.getTotal(), page.getResult());
    }
}
