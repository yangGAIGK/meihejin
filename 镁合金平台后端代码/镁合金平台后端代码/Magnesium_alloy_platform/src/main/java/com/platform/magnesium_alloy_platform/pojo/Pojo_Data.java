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
public class Pojo_Data {

    private int usefulCount;
    private int unusefulCount;
    private float percent;

    @Override
    //这个类实际上是重写Object类中的toString方法。默认情况下，
    // toString方法返回一个由类名、@符号和对象的哈希码（十六进制表示）组成的字符串。
    public String toString() {
        return "Data{" +
                "usefulCount=" + usefulCount +
                ", unusefulCount=" + unusefulCount +
                ", percent=" + percent +
                '}';
    }
}
