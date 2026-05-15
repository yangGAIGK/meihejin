package com.platform.magnesium_alloy_platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
// 应用Lombok的Data注释,为类生成各种遍历方法
@NoArgsConstructor
//为类生成一个无参构造函数
@AllArgsConstructor
//为类生成一个包含所有字段的构造函数

//可能代表遗传算法配置
public class GADTO {

    private double crossoverProbability;//交叉概率，表示在进行遗传算法时，个体之间进行基因交叉(交换部分基因)的概率
    private double variationProbability;//变异概率，表示在遗传算法中，个体基因发生变异（随机改变）的概率

}
