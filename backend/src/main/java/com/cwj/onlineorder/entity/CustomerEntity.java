package com.cwj.onlineorder.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * 顾客实体。
 *
 * 对应数据库 customers 表，存储注册用户的基本信息。
 * 使用 Java Record 实现，自动生成构造器、访问器、equals/hashCode/toString。
 *
 * 字段说明：
 * - id：主键，自增（BIGSERIAL）
 * - email：登录账号（唯一约束，存储时统一小写化）
 * - password：BCrypt 哈希后的密码（绝对不明文存储）
 * - enabled：账户是否启用（当前登录流程未检查此字段，仅作预留）
 * - firstName：名
 * - lastName：姓
 *
 * 设计考量：
 * - 使用 Record 而不是普通类，减少样板代码
 * - id 为 null 时表示新实体，Spring Data JDBC save() 会自动 INSERT
 * - id 有值时 save() 会自动 UPDATE
 *
 * @see com.cwj.onlineorder.repository.CustomerRepository
 * @see com.cwj.onlineorder.service.CustomerService
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
