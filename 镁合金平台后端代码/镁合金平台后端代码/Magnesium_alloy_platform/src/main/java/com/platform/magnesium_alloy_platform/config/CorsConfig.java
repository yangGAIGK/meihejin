package com.platform.magnesium_alloy_platform.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

//这个Java类 CorsConfig 使用了Spring框架的注解和配置特性，
// 用于配置跨源资源共享（CORS）的设置。
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("*"); // 允许所有域名访问，你也可以指定特定的域名
        config.addAllowedHeader("*"); // 允许所有请求头
        config.addAllowedMethod("*"); // 允许所有请求方法
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
