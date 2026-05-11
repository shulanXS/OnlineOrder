package com.cwj.onlineorder.model;

import com.cwj.onlineorder.entity.OrderDetailEntity;
import com.cwj.onlineorder.entity.OrderEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单数据传输对象（DTO）。
 *
 * 用于 API 响应的订单数据结构。
 *
 * 字段命名约定：
 * - 使用 Java camelCase 字段名
 * - Jackson 默认序列化为 camelCase JSON（如 totalPrice、createdAt）
 * - 前端 TypeScript 接口使用相同的 camelCase 命名保持一致
 *
 * 构造方式：
 * - 从 Entity + 明细列表构建（完整订单）—— 主要使用场景
 * - 仅从 Entity 构建（无明细，兜底用）—— 备用场景
 *
 * @param id         订单 ID
 * @param customerId  顾客 ID
 * @param status     订单状态（见 OrderService.VALID_STATUSES）
 * @param totalPrice 订单总价（快照结账时刻的购物车总价）
 * @param createdAt  下单时间
 * @param updatedAt  最后更新时间
 * @param details    订单明细列表
 */
public record OrderDto(
        Long id,
        Long customerId,
        String status,
        BigDecimal totalPrice,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<OrderDetailDto> details
) {
    /**
     * 仅包含基本信息的构造器（无明细）。
     * 用于兼容只需要订单基本信息而不需要明细的场景。
     *
     * @param id         订单 ID
     * @param customerId 顾客 ID
     * @param status     订单状态
     * @param totalPrice 订单总价
     * @param createdAt  下单时间
     * @param updatedAt  最后更新时间
     */
    public OrderDto(Long id, Long customerId, String status, BigDecimal totalPrice,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(id, customerId, status, totalPrice, createdAt, updatedAt, List.of());
    }

    /**
     * 从实体 + 明细列表构建完整的 DTO。
     *
     * @param entity         订单实体
     * @param detailEntities 明细实体列表
     */
    public OrderDto(OrderEntity entity, List<OrderDetailEntity> detailEntities) {
        this(
                entity.id(),
                entity.customerId(),
                entity.status(),
                entity.totalPrice(),
                entity.createdAt(),
                entity.updatedAt(),
                detailEntities.stream().map(OrderDetailDto::new).toList()
        );
    }
}
