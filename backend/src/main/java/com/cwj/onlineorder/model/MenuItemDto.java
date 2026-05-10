package com.cwj.onlineorder.model;

import com.cwj.onlineorder.entity.MenuItemEntity;
import java.math.BigDecimal;

public record MenuItemDto(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String imageUrl
) {
    public MenuItemDto(MenuItemEntity entity) {
        this(
                entity.id(),
                entity.name(),
                entity.description(),
                entity.price(),
                entity.imageUrl()
        );
    }
}
