package com.cwj.onlineorder.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

/**
 * 购物车商品行实体。
 *
 * 表示顾客购物车中的一行商品记录。
 *
 * 设计要点：
 * - 同一购物车中，同一菜品只占一行（通过 quantity 表示数量）
 * - price 是"快照"价格，记录添加时的菜品价格，不随菜品价格变动而变动
 *
 * 字段说明：
 * - id：主键，自增（BIGSERIAL）
 * - menuItemId：菜品 ID（FK -> menu_items.id）
 * - cartId：所属购物车 ID（FK -> carts.id）
 * - price：快照价格（添加时的单价，NUMERIC(19,4)）
 * - quantity：数量（>= 1）
 *
 * @see CartEntity 一个购物车包含多个 CartItemEntity
 * @see MenuItemEntity 菜品实体，关联到此行的 menuItemId
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
