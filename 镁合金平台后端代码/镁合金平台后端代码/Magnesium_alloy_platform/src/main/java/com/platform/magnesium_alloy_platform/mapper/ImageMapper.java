package com.platform.magnesium_alloy_platform.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ImageMapper {
    void insertImage(@Param("imageMap") Map<String, Object> imageMap);
    void deleteImagesByImageUrls(@Param("imageUrls") List<String> imageUrls);
    List<Map<String, Object>> queryImages(@Param("offset") int offset, @Param("pageSize") int pageSize);
    long countImages();
}
