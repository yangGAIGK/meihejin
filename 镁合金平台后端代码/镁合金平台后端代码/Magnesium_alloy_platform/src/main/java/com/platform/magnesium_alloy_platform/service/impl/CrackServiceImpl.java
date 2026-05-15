package com.platform.magnesium_alloy_platform.service.impl;

import com.platform.magnesium_alloy_platform.mapper.CrackMapper;
import com.platform.magnesium_alloy_platform.mapper.ImageMapper;
import com.platform.magnesium_alloy_platform.service.CrackService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CrackServiceImpl implements CrackService {

    private final ImageMapper imageMapper;
    private final CrackMapper crackMapper;

    public CrackServiceImpl(ImageMapper imageMapper, CrackMapper crackMapper) {
        this.imageMapper = imageMapper;
        this.crackMapper = crackMapper;
    }

    @Override
    public void insertCrackData(Map<String, Object> request, String uid) {
        String imageUrl = (String) request.get("ImageUrl");
        String binaryUrl = (String) request.get("BinaryUrl");

        Map<String, Object> imageMap = new HashMap<>();
        imageMap.put("imageUrl", imageUrl);
        imageMap.put("binaryUrl", binaryUrl);
        imageMap.put("createTime", LocalDateTime.now());
        imageMap.put("uid", uid);
        imageMapper.insertImage(imageMap);

        List<Map<String, Object>> crackInfos = (List<Map<String, Object>>) request.get("rows");
        for (Map<String, Object> crackInfo : crackInfos) {
            String crackStart = (String) crackInfo.get("CrackStart");
            double crackLength = Double.parseDouble(crackInfo.get("CrackLength").toString());
            double crackWidth = Double.parseDouble(crackInfo.get("CrackWidth").toString());
            double crackHeight = Double.parseDouble(crackInfo.get("CrackHeight").toString());
            double crackArea = Double.parseDouble(crackInfo.get("CrackArea").toString());
            double crackPerimeter = Double.parseDouble(crackInfo.get("CrackPerimeter").toString());

            Map<String, Object> crackMap = new HashMap<>();
            crackMap.put("crackStart", crackStart);
            crackMap.put("crackLength", crackLength);
            crackMap.put("crackWidth", crackWidth);
            crackMap.put("crackHeight", crackHeight);
            crackMap.put("crackArea", crackArea);
            crackMap.put("crackPerimeter", crackPerimeter);
            crackMap.put("imageUrl", imageUrl);
            crackMapper.insertCrack(crackMap);
        }
    }

    @Override
    public void deleteCrackData(List<String> imageUrls) {
        crackMapper.deleteCracksByImageUrls(imageUrls);
        imageMapper.deleteImagesByImageUrls(imageUrls);
    }

    @Override
    public Map<String, Object> queryImageData(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<Map<String, Object>> rows = imageMapper.queryImages(offset, pageSize);
        long total = imageMapper.countImages();
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("rows", rows);
        return result;
    }

    @Override
    public Map<String, Object> queryCrackData(String imageUrl) {
        List<Map<String, Object>> rows = crackMapper.queryCracksByImageUrl(imageUrl);
        int crackCount = rows.size();
        Map<String, Object> result = new HashMap<>();
        result.put("CrackCount", crackCount);
        result.put("rows", rows);
        return result;
    }
}
