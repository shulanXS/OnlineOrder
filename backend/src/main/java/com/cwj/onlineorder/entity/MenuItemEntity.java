package com.cwj.onlineorder.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

/**
 * 菜品实体。
 *
 * 表示一家餐厅的一道菜。
 *
 * 字段说明：
 * - id：主键，自增（BIGSERIAL）
 * - restaurantId：所属餐厅 ID（FK -> restaurants.id）
 * - name：菜品名称
 * - description：描述/配料说明
 * - price：单价（NUMERIC(19,4)，使用 BigDecimal 避免浮点精度问题）
 * - imageUrl：菜品图片 URL
 *
 * @see RestaurantEntity 餐厅实体，一个餐厅包含多个菜品
 */
@Table("menu_items")
public record MenuItemEntity(
        @Id Long id,
        Long restaurantId,
        String name,
        String description,
        BigDecimal price,
        String imageUrl
) {
}
