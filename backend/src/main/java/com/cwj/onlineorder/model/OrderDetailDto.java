package com.cwj.onlineorder.model;

import com.cwj.onlineorder.entity.OrderDetailEntity;
import java.math.BigDecimal;

/**
 * 订单明细数据传输对象。
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
