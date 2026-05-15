package com.platform.magnesium_alloy_platform.service.impl;

import com.platform.magnesium_alloy_platform.mapper.ParamInputMapper;
import com.platform.magnesium_alloy_platform.pojo.BPParameter;
import com.platform.magnesium_alloy_platform.pojo.PSOParameter;
import com.platform.magnesium_alloy_platform.pojo.GAParameter;
import com.platform.magnesium_alloy_platform.service.ParamInputService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParamInputServiceImpl implements ParamInputService {

    @Autowired
    private ParamInputMapper paramInputMapper;

    @Override
    public void insertBPParameter(BPParameter bpParameter) {
        // 确保 bpParameter 已经设置了正确的 createUser (UID)
        paramInputMapper.insertBPParameter(bpParameter);
    }

    @Override
    public void insertPSOParameter(PSOParameter psoParameter) {
        // 确保 psoParameter 已经设置了正确的 createUser (UID)
        paramInputMapper.insertPSOParameter(psoParameter);
    }

    @Override
    public void insertGAParameter(GAParameter gaParameter) {
        // 确保 gaParameter 已经设置了正确的 createUser (UID)
        paramInputMapper.insertGAParameter(gaParameter);
    }
}
