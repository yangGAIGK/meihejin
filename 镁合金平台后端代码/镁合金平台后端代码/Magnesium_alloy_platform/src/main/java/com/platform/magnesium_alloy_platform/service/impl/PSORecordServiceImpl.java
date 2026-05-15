package com.platform.magnesium_alloy_platform.service.impl;

import com.platform.magnesium_alloy_platform.mapper.PSORecordMapper;
import com.platform.magnesium_alloy_platform.service.PSORecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PSORecordServiceImpl implements PSORecordService {

    @Autowired
    private PSORecordMapper PSORecords;

    @Override
    public boolean deleteRecordsByIds(List<Integer> ids) {
        int affectedRows = PSORecords.deleteByIds(ids);
        return affectedRows > 0;
    }
}