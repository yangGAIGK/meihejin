package com.platform.magnesium_alloy_platform.interceptor;

import com.platform.magnesium_alloy_platform.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class JwtTokenInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String url = request.getRequestURL().toString();
        if (url.contains("login") ||url.contains("registerservlet")){
            return true;
        }
        String token = request.getHeader("Authorization");
//        if(!(StringUtils.hasLength(token))){
//            log.info("令牌不存在");
//            Result result = Result.error("NOT_LOGIN");
//            String json = JSONObject.toJSONString(result);
//            response.setContentType("application/json;charset=utf-8");
//            response.getWriter().write(json);
//            return false;
//        }
//        Map<String,Object> claims = JwtUtil.parseJWT(token);
//        ThreadLocalUtil.set(claims);
        return true;
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        ThreadLocalUtil.remove();
    }
}