package com.cwj.onlineorder.model;

import com.cwj.onlineorder.entity.CartItemEntity;
import com.cwj.onlineorder.entity.MenuItemEntity;
import java.math.BigDecimal;

/**
 * 购物车商品行数据传输对象。
 *
 * 从购物车商品行实体 + 菜品实体组合构建。
 * 包含菜品详细信息（名称、描述、图片 URL），便于前端直接展示。
 *
 * 字段命名：与前端 CartItem 接口一致，使用 camelCase。
 *
 * @param cartItemId         购物车商品行 ID
 * @param menuItemId         菜品 ID
 * @param restaurantId       所属餐厅 ID（从菜品实体获取）
 * @param price             快照单价
 * @param quantity          数量
 * @param menuItemName       菜品名称
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
    /**
     * 从商品行实体 + 菜品实体构建。
     *
     * restaurantId、name、description、imageUrl 均从菜品实体获取，
     * 因为这些信息可能随菜品数据更新而变化，需要快照。
     */
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
