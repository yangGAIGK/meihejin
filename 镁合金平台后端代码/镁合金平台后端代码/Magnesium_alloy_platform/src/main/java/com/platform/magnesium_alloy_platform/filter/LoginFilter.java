package com.platform.magnesium_alloy_platform.filter;

import com.alibaba.fastjson.JSONObject;
import com.platform.magnesium_alloy_platform.pojo.Result;
import com.platform.magnesium_alloy_platform.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
//@WebFilter(urlPatterns = "/*")
public class LoginFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        //1.请求获取url
        String url = req.getRequestURL().toString();
        //2.判断请求路径里面是否有login，有就跳出过滤器登录
        if(url.contains("login") ||url.contains("registerservlet")){
            filterChain.doFilter(req,resp);
            return;
        }

        //3.获得请求头中的令牌（token），没有login，访问的是其他的功能的网址，
        String token = req.getHeader("Authorization");
        //4.判断令牌是否存在，如果不存在，返回错误结果，没有登录
        if (!(StringUtils.hasLength(token))){
            log.info("令牌token不存在");
            //将错误信息转换为JSON格式并返回给前端
            Result result = Result.error("NOT_LOGIN");
            //把Result对象转换为JSON格式字符串 (fastjson是阿里巴巴提供的用于实现对象和json的转换工具类)
            String json = JSONObject.toJSONString(result);
            resp.setContentType("application/json;charset=utf-8");
            resp.getWriter().write(json);
            return;
        }
        //5.解析token，如果解析失败，返回错误结果
        try {
            JwtUtil.parseJWT(token);
        } catch (Exception e) {
            log.info("令牌token解析失败");
            //将错误信息转换为JSON格式并返回给前端
            Result result = Result.error("NOT_LOGIN");
            //把Result对象转换为JSON格式字符串 (fastjson是阿里巴巴提供的用于实现对象和json的转换工具类)
            String json = JSONObject.toJSONString(result);
            resp.setContentType("application/json;cahrset=utf-8");
            resp.getWriter().write(json);
            return;
        }
        filterChain.doFilter(req,resp);
    }

}
