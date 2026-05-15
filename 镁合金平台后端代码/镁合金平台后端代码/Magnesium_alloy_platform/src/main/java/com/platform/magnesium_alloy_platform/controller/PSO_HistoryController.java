package com.platform.magnesium_alloy_platform.controller;

import com.platform.magnesium_alloy_platform.service.PSO_HistoryService;
import com.platform.magnesium_alloy_platform.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/PSOHistory")
public class PSO_HistoryController {

    @Autowired
    private PSO_HistoryService historyPSOService;

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteHistory(@RequestBody Map<String, List<Integer>> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 检查 request 中是否包含 "ids" 键
            if (request == null ||!request.containsKey("ids")) {
                result.put("code", 0);
                result.put("msg", "Request does not contain 'ids' key.");
                result.put("data", null);
                return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
            }
            List<Integer> ids = request.get("ids");
            // 检查 ids 是否为 null
            if (ids == null) {
                result.put("code", 0);
                result.put("msg", "The 'ids' value is null.");
                result.put("data", null);
                return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
            }
            boolean success = historyPSOService.deleteByIds(ids);
            if (success) {
                result.put("code", 1);
                result.put("msg", "Records deleted successfully");
                result.put("data", null);
                return new ResponseEntity<>(result, HttpStatus.OK);
            } else {
                result.put("code", 0);
                result.put("msg", "Failed to delete records");
                result.put("data", null);
                return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            result.put("code", 0);
            result.put("msg", "An error occurred while deleting records: " + e.getMessage());
            result.put("data", null);
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/filter")
    public ResponseEntity<Map<String, Object>> filterPSOHistory(
            @RequestParam(required = false) Double minAveLength,
            @RequestParam(required = false) Double maxAveLength,
            @RequestParam(required = false) Double minLisanValue,
            @RequestParam(required = false) Double maxLisanValue,
            @RequestHeader("Authorization") String authorizationHeader
    ) {

        // 从 Authorization 头中提取 JWT token（假设格式为 "Bearer <token>"）
        String token = authorizationHeader.replace("Bearer ", "");

        // 获取当前用户的 UID，从 JWT token 中提取
        String uid = JwtUtil.getUidFromJwt(token);

        // 检查 uid 是否为 null 或空
        if (uid == null || uid.isEmpty()) {
            Map<String, Object> result1 = new HashMap<>();
            result1.put("code", 0);
            result1.put("msg", "failed");
            result1.put("data", null);
            return new ResponseEntity<>(result1, HttpStatus.valueOf("Invalid or expired JWT token."));
        }
        Map<String, Object> result = new HashMap<>();
        try {
            try {
                List<Map<String, Object>> data = historyPSOService.filterPSOHistory(uid,minAveLength, maxAveLength, minLisanValue, maxLisanValue);
                result.put("code", 1);
                result.put("msg", "success");
                Map<String, Object> dataResult = new HashMap<>();
                dataResult.put("total", data.size());
                dataResult.put("rows", data);
                result.put("data", dataResult);
                return new ResponseEntity<>(result, HttpStatus.OK);
            } catch (Exception e) {
                result.put("code", 0);
                result.put("msg", "failed");
                result.put("data", null);
                return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
