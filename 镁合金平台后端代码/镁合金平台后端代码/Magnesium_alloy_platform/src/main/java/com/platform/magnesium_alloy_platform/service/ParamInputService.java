package com.platform.magnesium_alloy_platform.service;

import com.platform.magnesium_alloy_platform.pojo.BPParameter;
import com.platform.magnesium_alloy_platform.pojo.PSOParameter;
import com.platform.magnesium_alloy_platform.pojo.GAParameter;

public interface ParamInputService {

	void insertBPParameter(BPParameter bpParameter);

	void insertPSOParameter(PSOParameter psoParameter);

	void insertGAParameter(GAParameter gaParameter);
}
