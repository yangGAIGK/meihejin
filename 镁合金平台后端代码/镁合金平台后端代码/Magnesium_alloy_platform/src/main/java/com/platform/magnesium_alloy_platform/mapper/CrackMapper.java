package com.platform.magnesium_alloy_platform.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface CrackMapper {
    void insertCrack(@Param("crackMap") Map<String, Object> crackMap);
    void deleteCracksByImageUrls(@Param("imageUrls") List<String> imageUrls);
    List<Map<String, Object>> queryCracksByImageUrl(@Param("imageUrl") String imageUrl);
}
