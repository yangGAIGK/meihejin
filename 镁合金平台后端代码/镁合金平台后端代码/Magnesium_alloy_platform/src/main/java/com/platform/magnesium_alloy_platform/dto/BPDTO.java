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

//代表反向传播（Back Propagation）神经网络配置
public class BPDTO {

    private int outputLayer; //输出层节点数，表示神经网络输出层的神经元数量。
    private int inputLayer; //输入层节点数，表示神经网络输入层的神经元数量。
    private int intermediateLayer;  //中间层节点数，表示神经网络中间层的神经元数量（如果只有一个中间层的话）。
    private String options; //选项，可能是一个字符串，包含神经网络的其他配置选项，如激活函数类型、网络结构细节等。
    private int numberOfCycles; //迭代次数或周期数，表示神经网络训练过程中进行的总迭代次数。
    private double learningRate;    //学习率，表示在神经网络训练过程中，用于调整权重和偏置的步长大小。
    private double errorTargetValue;    //误差目标值，表示神经网络训练的目标误差水平，当网络误差低于这个值时，训练可以停止。

}
