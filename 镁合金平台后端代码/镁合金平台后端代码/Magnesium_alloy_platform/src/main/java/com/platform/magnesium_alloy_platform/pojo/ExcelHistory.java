package com.platform.magnesium_alloy_platform.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExcelHistory {
    private Integer id;
    private double aveLength;
    private double lisanValue;
    private double hardness;
    private double yield_strength;
    private double strength_extension;

    // 修改 uid 字段类型为 String
    private String uid;  // 用户UID，替代原来的 Integer 类型

    @JsonFormat(pattern =  "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern =  "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Override
    public String toString() {
        return "ExcelHistory{" +
                "id=" + id +
                ", aveLength=" + aveLength +
                ", lisanValue=" + lisanValue +
                ", hardness=" + hardness +
                ", yield_strength=" + yield_strength +
                ", strength_extension=" + strength_extension +
                ", uid='" + uid + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
