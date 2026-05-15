package com.platform.magnesium_alloy_platform.controller;
import com.platform.magnesium_alloy_platform.service.BPRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/BPrecords")
public class BPRecordController {

    @Autowired
    private BPRecordService bpRecordsService;

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> batchDeleteBPRecords(@RequestBody Map<String, List<Integer>> request) {
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
            boolean success = bpRecordsService.deleteRecordsByIds(ids);
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
}