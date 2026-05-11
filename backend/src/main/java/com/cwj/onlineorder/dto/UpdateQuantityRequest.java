package com.cwj.onlineorder.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 更新购物车商品数量的请求 DTO。
 *
 * 字段说明：
 * - quantity：新的商品数量
 *
 * 校验规则：
 * - @NotNull：前端必须传入此字段
 * - @Min(0)：数量最小为 0（0 表示从购物车移除该商品）
 * - @Max(99)：数量最大为 99
 */
public record UpdateQuantityRequest(
        @NotNull(message = "数量不能为空")
        @Min(value = 0, message = "数量不能小于 0")
        @Max(value = 99, message = "数量不能超过 99")
        Integer quantity
) {}
