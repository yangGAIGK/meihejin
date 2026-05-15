package com.platform.magnesium_alloy_platform.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.platform.magnesium_alloy_platform.dto.PsoHistoryDTO;
import com.platform.magnesium_alloy_platform.mapper.PsoBpHistoryMapper;  // 使用 PsoBpHistoryMapper
import com.platform.magnesium_alloy_platform.pojo.PsoBpHistory;  // 使用 PsoBpHistory
import com.platform.magnesium_alloy_platform.pojo.PageResult;
import com.platform.magnesium_alloy_platform.service.PsoBpHistoryService;  // 使用 PsoBpHistoryService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PsoBpHistoryServiceImpl implements PsoBpHistoryService {

    @Autowired
    private PsoBpHistoryMapper psobpHistoryMapper;  // 修改为 psobpHistoryMapper

    @Override
    public void addHistory(PsoBpHistory psoBpHistory) {  // 使用 PsoBpHistory
        psoBpHistory.setCreateTime(LocalDateTime.now());
        psoBpHistory.setUpdateTime(LocalDateTime.now());
        psobpHistoryMapper.add(psoBpHistory); // 插入到 psobp_history 表
    }

    @Override
    public PageResult findHistory(PsoHistoryDTO psoHistoryDTO) {
        PageHelper.startPage(psoHistoryDTO.getPage(), psoHistoryDTO.getPageSize());
        Page<PsoBpHistory> page = psobpHistoryMapper.findHistory(psoHistoryDTO);  // 查询 psobp_history 表
        return new PageResult(page.getTotal(), page.getResult());
    }
}
