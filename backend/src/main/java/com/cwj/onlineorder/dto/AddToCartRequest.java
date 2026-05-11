package com.cwj.onlineorder.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 添加商品到购物车的请求 DTO。
 *
 * 字段说明：
 * - menuId：菜品 ID，不是 menuItemId（前端传入时字段名保持一致）
 *
 * 校验规则：
 * - @NotNull：前端必须传入此字段，否则返回 400 错误
 */
public record AddToCartRequest(
        @NotNull(message = "菜品 ID 不能为空")
        Long menuId
) {}
