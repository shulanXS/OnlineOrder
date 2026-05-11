package com.cwj.onlineorder.model;

import com.cwj.onlineorder.entity.MenuItemEntity;
import java.math.BigDecimal;

/**
 * 菜品数据传输对象。
 *
 * 字段命名：与前端 MenuItem 接口一致，使用 camelCase。
 *
 * @param id          菜品 ID
 * @param name        菜品名称
 * @param description 描述/配料说明
 * @param price       单价
 * @param imageUrl    图片 URL
 */
public record MenuItemDto(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String imageUrl
) {
    /**
     * 从菜品实体构建 DTO。
     */
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
