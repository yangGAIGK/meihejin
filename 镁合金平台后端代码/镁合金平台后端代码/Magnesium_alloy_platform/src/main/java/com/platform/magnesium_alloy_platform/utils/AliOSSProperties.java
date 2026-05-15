package com.platform.magnesium_alloy_platform.utils;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
//这个注解告诉Spring容器，这个类是一个组件，需要被Spring管理，并且可以被注入到其他需要它的Spring管理的Bean中。
@ConfigurationProperties(prefix = "aliyun.oss")
@Data
// 应用Lombok的Data注释,为类生成各种遍历方法
public class AliOSSProperties {

    private String endpoint;//阿里云OSS服务的终端节点，用于确定API请求发送到的位置
    private String accessKeyId;//访问阿里云服务的密钥ID
    private String accessKeySecret;//访问阿里云服务的API密钥的密钥
    private String bucketName;//在阿里云OSS中使用的存储空间（Bucket）的名称。

    @Override
    public String toString() {
        return "AliOSSProperties{" +
                "endpoint='" + endpoint + '\'' +
                ", accessKeyId='" + accessKeyId + '\'' +
                ", accessKeySecret='" + accessKeySecret + '\'' +
                ", bucketName='" + bucketName + '\'' +
                '}';
    }
}
