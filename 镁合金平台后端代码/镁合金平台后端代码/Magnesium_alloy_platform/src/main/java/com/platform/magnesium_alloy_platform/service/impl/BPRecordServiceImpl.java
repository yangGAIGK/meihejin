package com.platform.magnesium_alloy_platform.service.impl;

import com.platform.magnesium_alloy_platform.mapper.BPRecordMapper;
import com.platform.magnesium_alloy_platform.service.BPRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BPRecordServiceImpl implements BPRecordService {

    @Autowired
    private BPRecordMapper bpRecords;

    @Override
    public boolean deleteRecordsByIds(List<Integer> ids) {
        int affectedRows = bpRecords.deleteByIds(ids);
        return affectedRows > 0;
    }
}