package com.cwj.onlineorder.model;

import com.cwj.onlineorder.entity.OrderDetailEntity;
import com.cwj.onlineorder.entity.OrderEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单数据传输对象。
 * 包含订单基本信息及商品明细列表。
 *
 * @param id        订单 ID
 * @param customerId 顾客 ID
 * @param status    订单状态
 * @param totalPrice 订单总价
 * @param createdAt 下单时间
 * @param updatedAt 更新时间
 * @param details   订单明细列表
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
     * 仅订单基本信息（无明细，用于兼容旧接口）。
     */
    public OrderDto(Long id, Long customerId, String status, BigDecimal totalPrice,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(id, customerId, status, totalPrice, createdAt, updatedAt, List.of());
    }

    /**
     * 从实体转换，附带明细列表。
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

    /**
     * 从实体转换，无明细。
     */
    public OrderDto(OrderEntity entity) {
        this(entity.id(), entity.customerId(), entity.status(),
                entity.totalPrice(), entity.createdAt(), entity.updatedAt());
    }
}
