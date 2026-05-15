package com.platform.magnesium_alloy_platform.controller;

import com.platform.magnesium_alloy_platform.pojo.ResultT;
import com.platform.magnesium_alloy_platform.service.PSOBP_HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/PSOBPHistory")
public class PSOBP_HistoryController {

    @Autowired
    private PSOBP_HistoryService psobpHistoryService;

    @DeleteMapping("/delete")
    public ResultT<String> deleteByIds(@RequestBody Map<String, Object> request) {
        try {
            // 从 Map 中提取 ids 字段
            List<Integer> ids = (List<Integer>) request.get("ids");
            if (ids == null || ids.isEmpty()) {
                return new ResultT<>(0, "No IDs provided", null);
            }

            boolean success = psobpHistoryService.deleteByIds(ids);
            if (success) {
                return new ResultT<>(1, "Records deleted successfully", null);
            } else {
                return new ResultT<>(0, "Failed to delete records", null);
            }
        } catch (Exception e) {
            return new ResultT<>(0, "An error occurred while deleting records: " + e.getMessage(), null);
        }
    }
}
