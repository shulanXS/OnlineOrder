package com.cwj.onlineorder.repository;

import com.cwj.onlineorder.entity.MenuItemEntity;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

/**
 * 菜品 Repository。
 *
 * 提供菜品的 CRUD 操作。
 *
 * 方法说明：
 * - getByRestaurantId：查询指定餐厅的所有菜品（按 ID 升序）
 * - findAllByIdIn：批量查询多个 ID 的菜品（用于优化批量加载）
 */
public interface MenuItemRepository extends ListCrudRepository<MenuItemEntity, Long> {

    /**
     * 查询指定餐厅的所有菜品。
     *
     * @param restaurantId 餐厅 ID
     * @return 该餐厅的菜品列表
     */
    List<MenuItemEntity> getByRestaurantId(Long restaurantId);

    /**
     * 根据 ID 列表批量查询菜品。
     *
     * @param ids 菜品 ID 列表
     * @return 匹配的所有菜品
     */
    List<MenuItemEntity> findAllByIdIn(List<Long> ids);
}
