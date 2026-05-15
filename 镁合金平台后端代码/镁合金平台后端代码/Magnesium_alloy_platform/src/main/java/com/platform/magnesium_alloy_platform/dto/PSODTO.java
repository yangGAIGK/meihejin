package com.platform.magnesium_alloy_platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PSODTO {
    private int maxNum; // 最大迭代次数或者最大代数
    private int topValue; // 搜索空间的上界
    private int lowValue; // 搜索空间的下界
    private int swarmSize; // 群体大小
    private double individualFactor; // 个体因子
    private double groupFactor; // 群体因子
    private double inertiaFactor; // 惯性因子
    private String createUser; // 用于筛选数据的用户字段，应该从 JWT token 获取
    private int page; // 当前页码
    private int pageSize; // 每页显示条数
}
