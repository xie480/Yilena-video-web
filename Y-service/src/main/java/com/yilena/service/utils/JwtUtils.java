package com.yilena.service.utils;

import com.aliyuncs.utils.StringUtils;
import com.yilena.service.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final JwtProperties jwtProperties;

    // 静态变量存储配置
    private static  String secretKey;
    private static  long expire;

    @PostConstruct
    public void initStatic() {
        secretKey = jwtProperties.getSecretKey();
        expire = jwtProperties.getExpire();
        if (StringUtils.isEmpty(secretKey)) {
            throw new IllegalArgumentException("JWT密钥不能为空");
        }
    }

    // 静态方法使用静态配置
    public static String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .addClaims(claims)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .setExpiration(new Date(System.currentTimeMillis() + expire))
                .compact();
    }

    public static Claims parseToken(String jwt) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(jwt)
                .getBody();
    }
}