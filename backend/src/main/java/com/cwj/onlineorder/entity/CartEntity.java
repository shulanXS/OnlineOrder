package com.cwj.onlineorder.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

/**
 * 购物车实体。
 * 每个顾客有且仅有一个购物车（一对一关系）。
 *
 * 购物车本身只存储汇总信息（总价），
 * 实际商品明细存在 cart_items 表中。
 *
 * 字段说明：
 * - id：主键，自增
 * - customerId：所属顾客 ID（UNIQUE 约束，确保一人一车）
 * - totalPrice：购物车总价快照（使用 BigDecimal 避免浮点精度问题）
 */
@Table("carts")
public record CartEntity(
        @Id Long id,
        Long customerId,
        BigDecimal totalPrice
) {
}
