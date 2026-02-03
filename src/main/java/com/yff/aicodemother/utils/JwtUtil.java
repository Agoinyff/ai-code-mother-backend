package com.yff.aicodemother.utils;


import com.yff.aicodemother.exception.BusinessException;
import com.yff.aicodemother.exception.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * @author yff
 * @date 2026-02-03 10:03:51
 */
public class JwtUtil {


    private static final SecretKey secretKey = Keys
            .hmacShaKeyFor("a-very-long-secret-key-at-least-32-bytes!!!".getBytes());



    /**
     * 创建 Token
     */
    public static String createToken(Long userId) {
        return Jwts.builder()
                .expiration(new Date(System.currentTimeMillis() + 3600000*24*365L)) // 过期时间 1小时
                .subject("LOGIN_USER")
                .claim("userId", userId)
                .signWith(secretKey)
                .compact();
    }


    /**
     * 解析 Token 获取 userId
     */
    public static Long getUserId(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("userId", Long.class);
    }




    //解析 Token 看看是否合法
    public static void isTokenValid(String token) {

        if (token == null || token.isEmpty()) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"Token不能为空");
        }

        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"Token已过期");
        } catch (JwtException e) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"Token无效");
        }

    }







}
