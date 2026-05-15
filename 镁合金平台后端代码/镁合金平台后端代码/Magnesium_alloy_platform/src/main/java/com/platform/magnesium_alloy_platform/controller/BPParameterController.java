package com.platform.magnesium_alloy_platform.controller;

import com.platform.magnesium_alloy_platform.utils.JwtUtil;
import com.platform.magnesium_alloy_platform.pojo.BPParameter;
import com.platform.magnesium_alloy_platform.pojo.Result;
import com.platform.magnesium_alloy_platform.pojo.PageResult;
import com.platform.magnesium_alloy_platform.service.BPParameterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/BPrecords")
public class BPParameterController {

    @Autowired
    private BPParameterService bpParameterService;

    /**
     * 获取 BP 参数记录
     * @param request 请求体，包含分页参数
     * @param authorizationHeader 请求头中的 Authorization 字段
     * @return 返回 BP 参数记录的分页数据
     */
    @PostMapping
    public Result getBPParameters(@RequestBody Map<String, Integer> request, @RequestHeader("Authorization") String authorizationHeader) {
        // 从 Authorization 头中提取 JWT token（假设格式为 "Bearer <token>"）
        String token = authorizationHeader.replace("Bearer ", "");

        // 获取当前用户的 UID，从 JWT token 中提取
        String createUser = JwtUtil.getUidFromJwt(token);

        // 检查 createUser 是否为 null 或空
        if (createUser == null || createUser.isEmpty()) {
            return Result.error("Invalid or expired JWT token.");
        }

        log.info("Fetching BP records for user: {}", createUser);

        // 获取分页参数，默认第一页，每页 10 条数据
        int page = request.getOrDefault("page", 1); // 默认第一页
        int pageSize = request.getOrDefault("pageSize", 10); // 默认每页 10 条

        // 查询该用户的 BP 参数记录，获取响应的 Map 数据
        Map<String, Object> responseMap = bpParameterService.getBPParameters(page, pageSize, createUser);

        // 将 Map 中的数据提取并封装到 PageResult
        long total = ((Number) responseMap.get("total")).longValue();  // 将 total 转换为 long
        List<BPParameter> rows = (List<BPParameter>) responseMap.get("rows");  // 获取 rows

        // 创建 PageResult 对象并设置字段
        PageResult bpParameters = new PageResult(total, rows);

        // 返回查询结果
        return Result.success(bpParameters);
    }
}
