package com.cwj.onlineorder.model;

/**
 * 认证成功响应体。
 *
 * @param accessToken JWT 访问令牌
 * @param tokenType   令牌类型，固定为 "Bearer"
 * @param expiresIn   令牌有效期（秒）
 */
public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
