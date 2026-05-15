package com.platform.magnesium_alloy_platform.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.platform.magnesium_alloy_platform.dto.HistoryDTO;
import com.platform.magnesium_alloy_platform.mapper.HistoryMapper;
import com.platform.magnesium_alloy_platform.pojo.ExcelHistory;
import com.platform.magnesium_alloy_platform.pojo.PageResult;
import com.platform.magnesium_alloy_platform.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class HistoryServiceImpl implements HistoryService {

    @Autowired
    private HistoryMapper historyMapper;

    @Override
    public PageResult findHistory(HistoryDTO historyDTO) {
        // 打印日志确认 uid 是否正确
        System.out.println("Fetching history for UID: " + historyDTO.getUid());

        // 使用分页插件
        PageHelper.startPage(historyDTO.getPage(), historyDTO.getPageSize());

        // 查询历史记录
        Page<ExcelHistory> page = historyMapper.findHistory(historyDTO);

        // 返回分页结果
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void addHistory(ExcelHistory excelHistory) {
        // 设置创建时间和更新时间
        excelHistory.setCreateTime(LocalDateTime.now());
        excelHistory.setUpdateTime(LocalDateTime.now());

        // 插入历史记录
        historyMapper.add(excelHistory);
    }
}
