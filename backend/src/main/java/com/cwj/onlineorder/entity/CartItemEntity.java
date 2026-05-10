package com.cwj.onlineorder.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

/**
 * 购物车商品行实体。
 * 表示顾客购物车中的一个商品行。
 *
 * 设计要点：
 * - 同一购物车中，同一菜品只占一行（通过 quantity 表示数量）
 * - price 是"快照"价格，记录添加时的菜品价格，不随菜品价格变动而变动
 *
 * 字段说明：
 * - id：主键，自增
 * - menuItemId：菜品 ID（FK）
 * - cartId：所属购物车 ID（FK）
 * - price：快照价格（添加时的单价）
 * - quantity：数量（>= 1）
 */
@Table("cart_items")
public record CartItemEntity(
        @Id Long id,
        Long menuItemId,
        Long cartId,
        BigDecimal price,
        Integer quantity
) {
}
