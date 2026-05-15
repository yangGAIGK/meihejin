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
public class PsoBpHistory {

    private Integer id;

    private double aveLength;

    private double lisanValue;

    private double hardness;

    @JsonProperty("yield_strength")
    private double yieldStrength;

    @JsonProperty("strength_extension")
    private double strengthExtension;

    private int maxNum;

    @JsonProperty("topValue")
    private double topValue;

    @JsonProperty("lowValue")
    private double lowValue;

    @JsonProperty("swarmSize")
    private int swarmSize;

    @JsonProperty("individualFactor")
    private int individualFactor;

    @JsonProperty("groupFactor")
    private int groupFactor;

    @JsonProperty("inertiaFactor")
    private double inertiaFactor;

    private String uid;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
