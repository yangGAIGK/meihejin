package com.platform.magnesium_alloy_platform.utils;

public class ApiResponse {
    private int code;          // 状态码
    private String message;    // 提示信息

    public ApiResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    // Getters and Setters
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
