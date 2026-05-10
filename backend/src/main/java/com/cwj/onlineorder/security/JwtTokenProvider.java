package com.cwj.onlineorder.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token Provider.
 *
 * 负责 JWT Token 的生成、解析和校验。
 * 使用 HMAC-SHA256 算法对 Token 进行签名，确保其不可伪造。
 *
 * 设计要点：
 * - Token 中仅存储用户名（email），不存储敏感信息
 * - 包含过期时间，防止 Token 长期有效带来的安全风险
 * - 密钥从环境变量注入，支持运行时轮换
 *
 * @see JwtAuthFilter 用于在请求链路中校验 Token
 * @see SecurityConfig 用于配置 JWT FilterChain 集成
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    /**
     * Token 有效期：24 小时（毫秒）
     */
    private static final long EXPIRATION_MS = 24 * 60 * 60 * 1000L;

    /**
     * Token 前缀：Bearer
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * Authorization Header 键名
     */
    public static final String HEADER_STRING = "Authorization";

    private final SecretKey secretKey;

    public JwtTokenProvider(@Value("${jwt.secret}") String jwtSecret) {
        validateSecret(jwtSecret);
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    private void validateSecret(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT secret is not configured. Set the JWT_SECRET environment variable.");
        }
        if (secret.length() < 32) {
            throw new IllegalStateException(
                    "JWT secret must be at least 32 characters long for security. " +
                    "Set a strong secret via the JWT_SECRET environment variable.");
        }
        if (secret.contains("default") || secret.contains("change-in-production")) {
            throw new IllegalStateException(
                    "JWT secret appears to be a placeholder value. " +
                    "Set a strong secret via the JWT_SECRET environment variable.");
        }
    }

    /**
     * 根据认证信息生成 JWT Token。
     *
     * @param authentication Spring Security 认证对象（包含已加载的 UserDetails）
     * @return 签名后的 JWT Token 字符串
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateToken(userDetails.getUsername());
    }

    /**
     * 根据用户名生成 JWT Token。
     *
     * @param username 用户的邮箱（username）
     * @return 签名后的 JWT Token 字符串
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_MS);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 从 Token 字符串中提取用户名（subject）。
     *
     * @param token JWT Token 字符串（不含 Bearer 前缀）
     * @return 用户邮箱
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 解析并校验 Token，返回 Claims。
     *
     * @param token JWT Token 字符串
     * @return Token 的 Claims（包含 subject、过期时间等）
     * @throws JwtException Token 无效或已过期时抛出
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 校验 Token 是否有效。
     *
     * @param token JWT Token 字符串
     * @return true 表示 Token 有效且未过期；false 表示无效或已过期
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.warn("JWT Token 已过期: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.warn("JWT Token 格式错误: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.warn("JWT Token 不受支持: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("JWT Token 为空或格式不正确: {}", ex.getMessage());
        } catch (JwtException ex) {
            log.warn("JWT Token 校验失败: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * 获取 Token 剩余有效期（秒）。
     *
     * @param token JWT Token 字符串
     * @return 剩余秒数；若 Token 无效则返回 -1
     */
    public long getExpirationSeconds(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            return (expiration.getTime() - System.currentTimeMillis()) / 1000;
        } catch (JwtException ex) {
            return -1;
        }
    }
}
