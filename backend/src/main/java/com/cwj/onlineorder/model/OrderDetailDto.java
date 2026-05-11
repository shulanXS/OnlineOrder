package com.cwj.onlineorder.model;

import com.cwj.onlineorder.entity.OrderDetailEntity;
import java.math.BigDecimal;

/**
 * 订单明细数据传输对象。
 *
 * 记录订单中一个具体商品的快照信息。
 * 结账时从 cart_items 复制而来，后续菜品变更不影响已完成的订单。
 *
 * 字段命名：与前端 OrderDetail 接口一致，使用 camelCase。
 *
 * @param id               明细 ID
 * @param menuItemId       菜品 ID（快照）
 * @param menuItemName     菜品名称（快照）
 * @param menuItemDescription 菜品描述（快照）
 * @param menuItemImageUrl 菜品图片 URL（快照）
 * @param price            购买时的单价
 * @param quantity         购买数量
 */
public record OrderDetailDto(
        Long id,
        Long menuItemId,
        String menuItemName,
        String menuItemDescription,
        String menuItemImageUrl,
        BigDecimal price,
        Integer quantity
) {
    /**
     * 从实体构建 DTO（快照字段从实体复制）。
     */
    public OrderDetailDto(OrderDetailEntity entity) {
        this(
                entity.id(),
                entity.menuItemId(),
                entity.menuItemName(),
                entity.menuItemDescription(),
                entity.menuItemImageUrl(),
                entity.price(),
                entity.quantity()
        );
    }
}
