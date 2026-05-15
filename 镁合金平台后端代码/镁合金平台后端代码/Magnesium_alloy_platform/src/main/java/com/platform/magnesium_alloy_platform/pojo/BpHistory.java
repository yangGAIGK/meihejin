package com.platform.magnesium_alloy_platform.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BpHistory {
    private Integer id;
    private double aveLength;
    private double lisanValue;
    private double hardness;

    // 与数据库表列名一致，yield_strength对应yieldStrength
    private double yieldStrength; // 使用Java camelCase命名方式

    private double strengthExtension;
    private int inputLayer; // 使用int类型与数据库一致
    private int outputLayer; // 使用int类型与数据库一致
    private int intermediateLayer; // 使用int类型与数据库一致
    private String options;
    private int numberOfCycles;
    private double learningRate;
    private double errorTargetValue;
    private String uid;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
