package com.platform.magnesium_alloy_platform.controller;

import com.platform.magnesium_alloy_platform.pojo.PsoBpHistory;  // 使用 PsoBpHistory
import com.platform.magnesium_alloy_platform.pojo.Result;
import com.platform.magnesium_alloy_platform.service.PsoBpHistoryService;  // 使用 PsoBpHistoryService
import com.platform.magnesium_alloy_platform.utils.JwtUtil;
import com.platform.magnesium_alloy_platform.dto.PsoHistoryDTO;
import com.platform.magnesium_alloy_platform.pojo.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class PsoBpHistoryController {

    @Autowired
    private PsoBpHistoryService psobpHistoryService;  // 修改为 psobpHistoryService

    @PostMapping("/psobpparameter")
    public Result add(@RequestBody List<PsoBpHistory> psobpHistories, @RequestHeader("Authorization") String authorizationHeader) {
        log.info("接收到的PSO历史记录数据: {}", psobpHistories);

        // 提取JWT Token
        String token = authorizationHeader.replace("Bearer ", "");
        String currentUserId = JwtUtil.getUidFromJwt(token);

        if (currentUserId == null || currentUserId.isEmpty()) {
            return Result.error("Invalid or expired JWT token.");
        }

        for (PsoBpHistory psobpHistory : psobpHistories) {
            log.info("处理记录: {}", psobpHistory);

            if (psobpHistory.getYieldStrength() == 0.0 || psobpHistory.getStrengthExtension() == 0.0) {
                log.warn("发现异常数据: yieldStrength 或 strengthExtension 为 0.0, psobpHistory: {}", psobpHistory);
            }

            psobpHistory.setUid(currentUserId);  // 自动注入uid
            psobpHistoryService.addHistory(psobpHistory);  // 将数据存入数据库
        }

        return Result.success();
    }

    @PostMapping("/psobphistory")
    public Result findHistory(@RequestBody PsoHistoryDTO psoHistoryDTO, @RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        String currentUserId = JwtUtil.getUidFromJwt(token);

        if (currentUserId == null || currentUserId.isEmpty()) {
            return Result.error("Invalid or expired JWT token.");
        }

        psoHistoryDTO.setUid(currentUserId);  // 设置当前用户UID
        PageResult histories = psobpHistoryService.findHistory(psoHistoryDTO);  // 调用服务层查询历史记录
        return Result.success(histories);
    }
}
