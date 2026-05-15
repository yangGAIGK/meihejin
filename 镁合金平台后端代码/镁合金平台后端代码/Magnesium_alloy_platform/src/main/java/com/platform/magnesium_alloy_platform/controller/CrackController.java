package com.platform.magnesium_alloy_platform.controller;

import com.platform.magnesium_alloy_platform.service.CrackService;
import com.platform.magnesium_alloy_platform.utils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
public class CrackController {

    private final CrackService crackService;

    public CrackController(CrackService crackService) {
        this.crackService = crackService;
    }

    @PostMapping("/insertCrack")
    public ResponseEntity<Map<String, Object>> insertCrack(@RequestBody Map<String, Object> request,
                                                           @RequestHeader("Authorization") String authorizationHeader) {
        // 从 Authorization 头中提取 JWT token（假设格式为 "Bearer <token>"）
        String token = authorizationHeader.replace("Bearer ", "");

        // 获取当前用户的 UID，从 JWT token 中提取
        String uid = JwtUtil.getUidFromJwt(token);

        if (uid == null || uid.isEmpty()) {
            Map<String, Object> result1 = new HashMap<>();
            result1.put("code", 0);
            result1.put("msg", "failed: Invalid or expired JWT token.");
            result1.put("data", null);
            return new ResponseEntity<>(result1, HttpStatus.UNAUTHORIZED);
        }

        try {
            crackService.insertCrackData(request, uid);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 1);
            result.put("msg", "success");
            result.put("data", null);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 0);
            result.put("msg", "failed: " + e.getMessage());
            result.put("data", null);
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/deleteCrack")
    public ResponseEntity<Map<String, Object>> deleteCrack(
            @RequestParam("ImageUrls") List<String> imageUrls) {
        try {
            crackService.deleteCrackData(imageUrls);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 1);
            result.put("msg", "Records deleted successfully");
            result.put("data", null);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 0);
            result.put("msg", "failed: " + e.getMessage());
            result.put("data", null);
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/imageQuery")
    public ResponseEntity<Map<String, Object>> imageQuery(@RequestBody Map<String, Object> request) {
        int page = (int) request.get("page");
        int pageSize = (int) request.get("pageSize");
        try {
            Map<String, Object> queryResult = crackService.queryImageData(page, pageSize);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 1);
            result.put("msg", "success");
            result.put("data", queryResult);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 0);
            result.put("msg", "failed: " + e.getMessage());
            result.put("data", null);
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/crackQuery")
    public ResponseEntity<Map<String, Object>> crackQuery(@RequestBody Map<String, Object> request) {
        String imageUrl = (String) request.get("ImageUrl");
        try {
            Map<String, Object> queryResult = crackService.queryCrackData(imageUrl);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 1);
            result.put("msg", "success");
            result.put("data", queryResult);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 0);
            result.put("msg", "failed: " + e.getMessage());
            result.put("data", null);
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
