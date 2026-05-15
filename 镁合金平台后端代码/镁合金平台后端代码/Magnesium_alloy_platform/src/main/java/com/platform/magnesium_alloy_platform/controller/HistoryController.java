package com.platform.magnesium_alloy_platform.controller;

import com.platform.magnesium_alloy_platform.dto.HistoryDTO;
import com.platform.magnesium_alloy_platform.pojo.ExcelHistory;
import com.platform.magnesium_alloy_platform.pojo.PageResult;
import com.platform.magnesium_alloy_platform.pojo.Result;
import com.platform.magnesium_alloy_platform.service.HistoryService;
import com.platform.magnesium_alloy_platform.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class HistoryController {

    @Autowired
    private HistoryService historyService;

    @PostMapping("/history")
    public Result findHistory(@RequestBody HistoryDTO historyDTO, @RequestHeader("Authorization") String authorizationHeader) {
        // 从 Authorization 头中提取 JWT token（假设格式为 "Bearer <token>"）
        String token = authorizationHeader.replace("Bearer ", "");

        // 获取当前用户的 UID，从 JWT token 中提取
        String currentUserId = JwtUtil.getUidFromJwt(token);

        // 检查 currentUserId 是否为 null 或空
        if (currentUserId == null || currentUserId.isEmpty()) {
            return Result.error("Invalid or expired JWT token.");
        }

        // 将提取到的 UID 设置到 HistoryDTO 中
        historyDTO.setUid(currentUserId);

        // 查询该用户的历史记录
        PageResult histories = historyService.findHistory(historyDTO);

        // 返回查询结果
        return Result.success(histories);
    }

    @PostMapping("/parameter")
    public Result add(@RequestBody ExcelHistory excelHistory, @RequestHeader("Authorization") String authorizationHeader) {
        log.info("新增历史记录: {}", excelHistory);

        // 从 Authorization 头中提取 JWT token（假设格式为 "Bearer <token>"）
        String token = authorizationHeader.replace("Bearer ", "");

        // 获取当前用户的 UID 从 JWT token 中提取
        String currentUserId = JwtUtil.getUidFromJwt(token);

        // 检查 currentUserId 是否为 null 或空
        if (currentUserId == null || currentUserId.isEmpty()) {
            return Result.error("Invalid or expired JWT token.");
        }

        try {
            // 将当前用户的 UID 设置到 excelHistory 对象
            excelHistory.setUid(currentUserId);  // 设置 UID 动态
        } catch (Exception e) {
            // 捕获异常并返回错误信息
            return Result.error("Failed to set UID in the record.");
        }

        // 调用 service 层新增历史记录
        historyService.addHistory(excelHistory);

        // 返回成功响应
        return Result.success();
    }
}
