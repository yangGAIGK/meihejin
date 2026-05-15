package com.platform.magnesium_alloy_platform.utils;

public class Response {
    private int code;
    private String msg;
    private Object data;

    // 保留原有构造器和代码
    public Response(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // 新增静态方法，不影响现有代码
    public static Response success(Object data) {
        return new Response(1, "success", data);
    }

    public static Response failure(String msg) {
        return new Response(0, msg, null);
    }

    // Getter 和 Setter 方法
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
