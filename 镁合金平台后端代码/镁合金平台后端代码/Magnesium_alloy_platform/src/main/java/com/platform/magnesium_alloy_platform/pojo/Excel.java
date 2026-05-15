package com.platform.magnesium_alloy_platform.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
// 应用Lombok的Data注释,为类生成各种遍历方法
@NoArgsConstructor
//为类生成一个无参构造函数
@AllArgsConstructor
//为类生成一个包含所有字段的构造函数
public class Excel {

    private Double aveLength;
    private Double lisanValue;

    @Override
    public String toString() {
        return "Excel{" +
                "aveLength=" + aveLength +
                ", lisanValue=" + lisanValue +
                '}';
    }
}
