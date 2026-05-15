package com.platform.magnesium_alloy_platform.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PsoHistory {

    private Integer id;

    private double aveLength;

    private double lisanValue;

    private double hardness;

    @JsonProperty("yield_strength")  // 映射前端的 yield_strength 字段
    private double yieldStrength;

    @JsonProperty("strength_extension")  // 映射前端的 strength_extension 字段
    private double strengthExtension;

    private int maxNum;

    @JsonProperty("topValue")  // 映射前端的 topValue 字段
    private double topValue;

    @JsonProperty("lowValue")  // 映射前端的 lowValue 字段
    private double lowValue;

    @JsonProperty("swarmSize")  // 映射前端的 swarmSize 字段
    private int swarmSize;

    @JsonProperty("individualFactor")  // 映射前端的 individualFactor 字段
    private int individualFactor;

    @JsonProperty("groupFactor")  // 映射前端的 groupFactor 字段
    private int groupFactor;

    @JsonProperty("inertiaFactor")  // 映射前端的 inertiaFactor 字段
    private double inertiaFactor;

    private String uid;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
