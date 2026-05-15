package com.platform.magnesium_alloy_platform.config;


import com.platform.magnesium_alloy_platform.interceptor.JwtTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 实现WebMvcConfigurer接口，重写addInterceptors注册以配置拦截器
 * 注册拦截器
 * “/**”代表拦截所有的网站
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private JwtTokenInterceptor jwtTokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {//注册自定义的拦截器对象
        registry.addInterceptor(jwtTokenInterceptor).addPathPatterns("/**")
                .excludePathPatterns("/login","/registerservlet");//要拦截的路径是所有的路径
    }
}
