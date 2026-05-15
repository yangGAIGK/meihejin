package com.platform.magnesium_alloy_platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoryDTO implements Serializable {
    private int page;          // 当前页码
    private int pageSize;      // 每页显示的记录数
    private String uid;        // 用户 UID，用于过滤历史记录
}
