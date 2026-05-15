package com.platform.magnesium_alloy_platform.controller;

import com.platform.magnesium_alloy_platform.pojo.BPParameter;
import com.platform.magnesium_alloy_platform.pojo.PSOParameter;
import com.platform.magnesium_alloy_platform.pojo.GAParameter;
import com.platform.magnesium_alloy_platform.service.ParamInputService;
import com.platform.magnesium_alloy_platform.utils.JwtUtil;
import com.platform.magnesium_alloy_platform.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/parameter")
public class ParamInputController {

	@Autowired
	private ParamInputService paramInputService;

	// 插入 BP 参数
	@PostMapping("/BP")
	public ResponseEntity<?> insertBPParameter(@RequestBody BPParameter bpParameter, @RequestHeader("Authorization") String authorizationHeader) {
		try {
			// 从 Authorization 头中提取 JWT token（假设格式为 "Bearer <token>"）
			String token = authorizationHeader.replace("Bearer ", "");

			// 获取当前用户的 UID 从 JWT token 中提取
			String currentUserId = JwtUtil.getUidFromJwt(token);

			// 检查 currentUserId 是否为 null 或空
			if (currentUserId == null || currentUserId.isEmpty()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(new ApiResponse(0, "Invalid or expired JWT token."));
			}

			// 将当前用户的 UID 设置到 bpParameter 对象
			bpParameter.setCreateUser(currentUserId);

			// 调用服务层插入 BP 参数
			paramInputService.insertBPParameter(bpParameter);
			return ResponseEntity.ok(new ApiResponse(1, "BP parameters inserted successfully"));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(0, "Failed to insert BP parameters: " + e.getMessage()));
		}
	}

	// 插入 PSO 参数
	@PostMapping("/PSO")
	public ResponseEntity<?> insertPSOParameter(@RequestBody PSOParameter psoParameter, @RequestHeader("Authorization") String authorizationHeader) {
		try {
			// 从 Authorization 头中提取 JWT token
			String token = authorizationHeader.replace("Bearer ", "");

			// 获取当前用户的 UID 从 JWT token 中提取
			String currentUserId = JwtUtil.getUidFromJwt(token);

			// 检查 currentUserId 是否为 null 或空
			if (currentUserId == null || currentUserId.isEmpty()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(new ApiResponse(0, "Invalid or expired JWT token."));
			}

			// 将当前用户的 UID 设置到 psoParameter 对象
			psoParameter.setCreateUser(currentUserId);

			// 调用服务层插入 PSO 参数
			paramInputService.insertPSOParameter(psoParameter);
			return ResponseEntity.ok(new ApiResponse(1, "PSO parameters inserted successfully"));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(0, "Failed to insert PSO parameters: " + e.getMessage()));
		}
	}

	// 插入 GA 参数
	@PostMapping("/GA")
	public ResponseEntity<?> insertGAParameter(@RequestBody GAParameter gaParameter, @RequestHeader("Authorization") String authorizationHeader) {
		try {
			// 从 Authorization 头中提取 JWT token
			String token = authorizationHeader.replace("Bearer ", "");

			// 获取当前用户的 UID 从 JWT token 中提取
			String currentUserId = JwtUtil.getUidFromJwt(token);

			// 检查 currentUserId 是否为 null 或空
			if (currentUserId == null || currentUserId.isEmpty()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(new ApiResponse(0, "Invalid or expired JWT token."));
			}

			// 将当前用户的 UID 设置到 gaParameter 对象
			gaParameter.setCreateUser(currentUserId);

			// 调用服务层插入 GA 参数
			paramInputService.insertGAParameter(gaParameter);
			return ResponseEntity.ok(new ApiResponse(1, "GA parameters inserted successfully"));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(0, "Failed to insert GA parameters: " + e.getMessage()));
		}
	}
}
