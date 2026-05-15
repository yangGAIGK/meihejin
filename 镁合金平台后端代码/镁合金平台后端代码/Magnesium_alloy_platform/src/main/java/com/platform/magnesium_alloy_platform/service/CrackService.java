package com.platform.magnesium_alloy_platform.service;

import java.util.List;
import java.util.Map;

public interface CrackService {
    void insertCrackData(Map<String, Object> request, String uid);
    void deleteCrackData(List<String> imageUrls);
    Map<String, Object> queryImageData(int page, int pageSize);
    Map<String, Object> queryCrackData(String imageUrl);
}
