package com.cwj.onlineorder.model;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求体。
 *
 * 字段说明：
 * - email：用户邮箱（登录用户名）
 * - password：密码
 *
 * @see com.cwj.onlineorder.controller.AuthController#login(LoginRequest)
 */
public record LoginRequest(
        @NotBlank(message = "用户名不能为空")
        String email,

        @NotBlank(message = "密码不能为空")
        String password
) {
}
