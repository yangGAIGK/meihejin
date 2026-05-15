package com.platform.magnesium_alloy_platform.controller;

import com.platform.magnesium_alloy_platform.dto.BpHistoryDTO;
import com.platform.magnesium_alloy_platform.pojo.BpHistory;
import com.platform.magnesium_alloy_platform.pojo.PageResult;
import com.platform.magnesium_alloy_platform.pojo.Result;
import com.platform.magnesium_alloy_platform.service.BpHistoryService;
import com.platform.magnesium_alloy_platform.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class BpHistoryController {

    @Autowired
    private BpHistoryService bpHistoryService;

    @PostMapping("/bphistory")
    public Result findHistory(@RequestBody BpHistoryDTO bpHistoryDTO, @RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        String currentUserId = JwtUtil.getUidFromJwt(token);

        if (currentUserId == null || currentUserId.isEmpty()) {
            return Result.error("Invalid or expired JWT token.");
        }

        bpHistoryDTO.setUid(currentUserId);
        PageResult histories = bpHistoryService.findHistory(bpHistoryDTO);
        return Result.success(histories);
    }

    @PostMapping("/bpparameter")
    public Result add(@RequestBody List<BpHistory> bpHistories, @RequestHeader("Authorization") String authorizationHeader) {
        log.info("批量新增历史记录: {}", bpHistories);

        String token = authorizationHeader.replace("Bearer ", "");
        String currentUserId = JwtUtil.getUidFromJwt(token);

        if (currentUserId == null || currentUserId.isEmpty()) {
            return Result.error("Invalid or expired JWT token.");
        }

        for (BpHistory bpHistory : bpHistories) {
            bpHistory.setUid(currentUserId);
            bpHistoryService.addHistory(bpHistory);
        }
        return Result.success();
    }
}
