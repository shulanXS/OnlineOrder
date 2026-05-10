package com.cwj.onlineorder.model;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求体。
 *
 * @param username 用户邮箱（即登录用户名）
 * @param password 密码
 */
public record LoginRequest(
        @NotBlank(message = "用户名不能为空")
        String username,

        @NotBlank(message = "密码不能为空")
        String password
) {
}
