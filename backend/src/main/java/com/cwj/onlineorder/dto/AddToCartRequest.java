package com.cwj.onlineorder.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 添加商品到购物车的请求 DTO。
 *
 * 字段说明：
 * - menuId：菜品 ID（对应 menu_items 表的主键）
 *
 * 字段命名说明：
 * - 字段名为 menuId 而非 menuItemId，以保持与前端传入 JSON 的一致性
 * - 前端 AddToCartRequest 在序列化为 JSON 时使用 camelCase，最终为 { menuId: ... }
 * - 后端反序列化时需要匹配的字段名（Jackson 默认使用 Java 字段名）
 *
 * 校验规则：
 * - @NotNull：前端必须传入此字段，否则返回 400 Bad Request
 * - 建议配合 @Positive 使用，但后端在 MenuItemService 中会二次校验菜品是否存在
 *
 * @see com.cwj.onlineorder.controller.CartController#addToCart(AddToCartRequest)
 */
public record AddToCartRequest(
        @NotNull(message = "菜品 ID 不能为空")
        Long menuId
) {}
