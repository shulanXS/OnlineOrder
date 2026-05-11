package com.cwj.onlineorder.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体。
 * 表示用户完成结账后生成的订单记录。
 *
 * 字段说明：
 * - id：主键，自增
 * - customerId：下单顾客 ID
 * - status：订单状态（PENDING / CONFIRMED / PREPARING / SHIPPING / COMPLETED / CANCELLED）
 * - totalPrice：订单总价（快照结账时刻的购物车总价）
 * - createdAt：下单时间
 * - updatedAt：最后更新时间
 */
@Table("orders")
public record OrderEntity(
        @Id Long id,
        Long customerId,
        String status,
        BigDecimal totalPrice,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
