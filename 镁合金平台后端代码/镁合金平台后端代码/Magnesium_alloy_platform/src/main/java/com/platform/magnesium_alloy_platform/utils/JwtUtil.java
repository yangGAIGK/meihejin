package com.platform.magnesium_alloy_platform.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JwtUtil {
    private static String signKey = "wcitma";  // 密钥

    // 生成 JWT
    public static String generateJwt(String uid) {
        return Jwts.builder()
                .claim("uid", uid)  // 使用 uid 作为字段名称
                .signWith(SignatureAlgorithm.HS256, signKey)
                .setExpiration(new Date(System.currentTimeMillis() + 43200000))  // 设置过期时间
                .compact();
    }

    // 从 JWT 中提取 uid
    public static String getUidFromJwt(String token) {
        try {
            Claims claims = parseJWT(token);  // 解析 JWT
            return claims.get("uid", String.class);  // 获取 uid 字段
        } catch (Exception e) {
            return null;  // 如果解析失败，返回 null
        }
    }

    // 解析 JWT 并返回 Claims 对象
    public static Claims parseJWT(String jwt) {
        Claims claims = Jwts.parser()
                .setSigningKey(signKey)  // 使用签名密钥
                .parseClaimsJws(jwt)  // 解析 JWT
                .getBody();  // 获取有效载荷
        return claims;
    }
}