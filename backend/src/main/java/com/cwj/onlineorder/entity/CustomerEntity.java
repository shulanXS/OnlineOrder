package com.cwj.onlineorder.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * 顾客实体。
 * 对应数据库 customers 表，存储注册用户的基本信息。
 *
 * 使用 Java Record 实现：自动生成构造器、访问器、equals/hashCode/toString。
 *
 * 字段说明：
 * - id：主键，自增
 * - email：登录账号（唯一）
 * - password：BCrypt 哈希后的密码（绝对不明文存储）
 * - enabled：账户是否启用（false 可实现软禁用）
 * - firstName：名
 * - lastName：姓
 */
@Table("customers")
public record CustomerEntity(
        @Id Long id,
        String email,
        String password,
        boolean enabled,
        String firstName,
        String lastName
) {
}
