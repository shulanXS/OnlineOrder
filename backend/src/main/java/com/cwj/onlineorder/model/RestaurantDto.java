package com.cwj.onlineorder.model;

import com.cwj.onlineorder.entity.RestaurantEntity;
import java.util.List;

/**
 * 餐厅数据传输对象。
 *
 * 字段命名：与前端 Restaurant 接口一致，使用 camelCase。
 *
 * @param id        餐厅 ID
 * @param name      餐厅名称
 * @param address   餐厅地址
 * @param phone     联系电话
 * @param imageUrl  餐厅封面图 URL
 * @param menuItems 菜品列表（嵌套结构，避免前端多次请求）
 */
public record RestaurantDto(
        Long id,
        String name,
        String address,
        String phone,
        String imageUrl,
        List<MenuItemDto> menuItems
) {
    /**
     * 从餐厅实体 + 菜品列表构建 DTO。
     *
     * @param entity   餐厅实体
     * @param menuItems 菜品 DTO 列表
     */
    public RestaurantDto(RestaurantEntity entity, List<MenuItemDto> menuItems) {
        this(
                entity.id(),
                entity.name(),
                entity.address(),
                entity.phone(),
                entity.imageUrl(),
                menuItems
        );
    }
}
