package com.platform.magnesium_alloy_platform.controller;

import com.platform.magnesium_alloy_platform.pojo.PsoHistory;
import com.platform.magnesium_alloy_platform.pojo.Result;
import com.platform.magnesium_alloy_platform.service.PsoHistoryService;
import com.platform.magnesium_alloy_platform.utils.JwtUtil;
import com.platform.magnesium_alloy_platform.dto.PsoHistoryDTO;  // 引入PsoHistoryDTO
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
public class PsoHistoryController {

    @Autowired
    private PsoHistoryService psoHistoryService;

    @PostMapping("/psoparameter")
    public Result add(@RequestBody List<PsoHistory> psoHistories, @RequestHeader("Authorization") String authorizationHeader) {
        log.info("接收到的PSO历史记录数据: {}", psoHistories);

        // 提取JWT Token
        String token = authorizationHeader.replace("Bearer ", "");
        String currentUserId = JwtUtil.getUidFromJwt(token);

        if (currentUserId == null || currentUserId.isEmpty()) {
            return Result.error("Invalid or expired JWT token.");
        }

        // 遍历每条历史记录并设置uid
        for (PsoHistory psoHistory : psoHistories) {
            // 添加日志，检查字段值
            log.info("处理记录: {}", psoHistory);

            // 确保字段值正确传递
            if (psoHistory.getYieldStrength() == 0.0 || psoHistory.getStrengthExtension() == 0.0) {
                log.warn("发现异常数据: yieldStrength 或 strengthExtension 为 0.0, psoHistory: {}", psoHistory);
            }

            psoHistory.setUid(currentUserId);  // 自动注入uid
            psoHistoryService.addHistory(psoHistory);  // 将数据存入数据库
        }

        return Result.success();
    }

    // 新增查询接口
    @PostMapping("/psohistory")
    public Result findHistory(@RequestBody PsoHistoryDTO psoHistoryDTO, @RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        String currentUserId = JwtUtil.getUidFromJwt(token);

        if (currentUserId == null || currentUserId.isEmpty()) {
            return Result.error("Invalid or expired JWT token.");
        }

        psoHistoryDTO.setUid(currentUserId);  // 设置当前用户UID
        PageResult histories = psoHistoryService.findHistory(psoHistoryDTO);  // 调用服务层查询历史记录
        return Result.success(histories);
    }
}
