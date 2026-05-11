package com.cwj.onlineorder.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户注册请求体。
 *
 * @param email     邮箱（唯一）
 * @param password  密码（至少 8 位）
 * @param firstName 名
 * @param lastName  姓
 */
public record RegisterBody(
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        String email,

        @NotBlank(message = "密码不能为空")
        @Size(min = 8, message = "密码至少8位")
        String password,

        @NotBlank(message = "名字不能为空")
        @Size(max = 50, message = "名字不能超过50个字符")
        String firstName,

        @NotBlank(message = "姓氏不能为空")
        @Size(max = 50, message = "姓氏不能超过50个字符")
        String lastName
) {
}
