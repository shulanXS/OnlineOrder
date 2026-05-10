package com.cwj.onlineorder.model;

import com.cwj.onlineorder.entity.CartItemEntity;
import com.cwj.onlineorder.entity.MenuItemEntity;
import java.math.BigDecimal;

/**
 * 购物车商品行数据传输对象。
 *
 * @param cartItemId   购物车商品行 ID
 * @param menuItemId   菜品 ID
 * @param restaurantId  所属餐厅 ID
 * @param price        快照单价
 * @param quantity     数量
 * @param menuItemName        菜品名称
 * @param menuItemDescription 菜品描述
 * @param menuItemImageUrl   菜品图片 URL
 */
public record CartItemDto(
        Long cartItemId,
        Long menuItemId,
        Long restaurantId,
        BigDecimal price,
        Integer quantity,
        String menuItemName,
        String menuItemDescription,
        String menuItemImageUrl
) {
    public CartItemDto(CartItemEntity cartItemEntity, MenuItemEntity menuItemEntity) {
        this(
                cartItemEntity.id(),
                cartItemEntity.menuItemId(),
                menuItemEntity.restaurantId(),
                cartItemEntity.price(),
                cartItemEntity.quantity(),
                menuItemEntity.name(),
                menuItemEntity.description(),
                menuItemEntity.imageUrl()
        );
    }
}
