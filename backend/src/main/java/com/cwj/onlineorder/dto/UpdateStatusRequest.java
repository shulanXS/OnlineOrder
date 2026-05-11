package com.cwj.onlineorder.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 更新订单状态的请求 DTO。
 *
 * 字段说明：
 * - status：新的订单状态值
 *
 * 校验规则：
 * - @NotBlank：状态不能为空字符串
 * - 具体状态值的合法性在 OrderService 层校验
 */
public record UpdateStatusRequest(
        @NotBlank(message = "状态不能为空")
        String status
) {}
