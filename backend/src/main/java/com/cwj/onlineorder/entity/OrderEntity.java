package com.cwj.onlineorder.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体。
 *
 * 表示用户完成结账后生成的订单记录。
 * 订单一旦创建，其内容（商品明细）就是结账时刻的快照，不会随菜品信息变化而变化。
 *
 * 字段说明：
 * - id：主键，自增（BIGSERIAL）
 * - customerId：下单顾客 ID
 * - status：订单状态（PENDING / CONFIRMED / PREPARING / SHIPPING / COMPLETED / CANCELLED）
 * - totalPrice：订单总价（快照结账时刻的购物车总价）
 * - createdAt：下单时间（由应用层在创建时注入）
 * - updatedAt：最后更新时间（由应用层在更新时注入）
 *
 * 状态流转规则（见 OrderService）：
 * - 正向流转：PENDING -> CONFIRMED -> PREPARING -> SHIPPING -> COMPLETED
 * - 终态：COMPLETED / CANCELLED 不可再修改
 * - 任意非终态可直接跳转到终态
 *
 * @see com.cwj.onlineorder.repository.OrderRepository
 * @see com.cwj.onlineorder.service.OrderService
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
