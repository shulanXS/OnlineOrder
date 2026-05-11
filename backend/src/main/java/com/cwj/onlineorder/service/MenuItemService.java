package com.cwj.onlineorder.service;

import com.cwj.onlineorder.entity.MenuItemEntity;
import com.cwj.onlineorder.repository.MenuItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 菜品服务层。
 *
 * 负责处理菜品相关的业务逻辑。
 *
 * 设计要点：
 * - 菜品数据变化不频繁，通过 @Cacheable 缓存减少数据库查询
 * - 目前仅支持按餐厅 ID 查询菜品列表
 */
@Service
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;

    public MenuItemService(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    /**
     * 根据餐厅 ID 查询该餐厅的所有菜品。
     *
     * @param restaurantId 餐厅 ID
     * @return 该餐厅的菜品列表（按 ID 升序排列）
     */
    public List<MenuItemEntity> getMenuItemsByRestaurantId(long restaurantId) {
        return menuItemRepository.getByRestaurantId(restaurantId);
    }
}
