package com.platform.magnesium_alloy_platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PsoHistoryDTO implements Serializable {
    private int page;
    private int pageSize;
    private String uid;  // 用户ID
}
