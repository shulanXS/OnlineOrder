package com.cwj.onlineorder.model;

/**
 * 当前登录用户信息响应体。
 *
 * @param id        用户 ID
 * @param email     邮箱
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
