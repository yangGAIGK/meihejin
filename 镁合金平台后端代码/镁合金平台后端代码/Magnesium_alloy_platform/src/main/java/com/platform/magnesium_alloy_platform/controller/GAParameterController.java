package com.platform.magnesium_alloy_platform.controller;

import com.platform.magnesium_alloy_platform.service.GAParameterService;
import com.platform.magnesium_alloy_platform.utils.JwtUtil;
import com.platform.magnesium_alloy_platform.pojo.Result;
import com.platform.magnesium_alloy_platform.pojo.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/GArecords")
public class GAParameterController {

    @Autowired
    private GAParameterService gaParameterService;

    /**
     * 获取 GA 参数记录
     * @param request 请求体，包含分页参数
     * @param authorizationHeader 请求头中的 Authorization 字段
     * @return 返回 GA 参数记录的分页数据
     */
    @PostMapping
    public Result getGARecords(@RequestBody Map<String, Integer> request, @RequestHeader("Authorization") String authorizationHeader) {
        // 从 Authorization 头中提取 JWT token（假设格式为 "Bearer <token>"）
        String token = authorizationHeader.replace("Bearer ", "");

        // 获取当前用户的 UID，从 JWT token 中提取
        String createUser = JwtUtil.getUidFromJwt(token);

        // 检查 createUser 是否为 null 或空
        if (createUser == null || createUser.isEmpty()) {
            return Result.error("Invalid or expired JWT token.");
        }

        log.info("Fetching GA records for user: {}", createUser);

        // 获取分页参数，默认第一页，每页 10 条数据
        int page = request.getOrDefault("page", 1); // 默认第一页
        int pageSize = request.getOrDefault("pageSize", 10); // 默认每页 10 条

        // 查询该用户的 GA 参数记录
        Map<String, Object> resultMap = gaParameterService.getGAParameters(page, pageSize, createUser);

        // 获取分页数据
        PageResult pageResult = new PageResult();
        Object totalObj = resultMap.get("total");

        // 确保 total 是 Long 类型
        long total = (totalObj instanceof Integer) ? ((Integer) totalObj).longValue() : (Long) totalObj;
        pageResult.setTotal(total); // 设置总记录数
        pageResult.setRecords((List) resultMap.get("rows")); // 设置当前页的记录列表

        // 返回结果
        return Result.success(pageResult);
    }
}
