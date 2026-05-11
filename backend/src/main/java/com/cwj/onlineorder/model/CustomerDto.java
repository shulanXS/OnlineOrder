package com.cwj.onlineorder.model;

/**
 * 当前登录用户信息响应体。
 *
 * 字段命名：与前端 User 接口一致，使用 camelCase。
 *
 * @param id        用户 ID
 * @param email    邮箱（登录用户名）
 * @param firstName 名
 * @param lastName  姓
 */
public record CustomerDto(
        Long id,
        String email,
        String firstName,
        String lastName
) {
}
