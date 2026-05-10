package com.cwj.onlineorder.service;

import com.cwj.onlineorder.entity.MenuItemEntity;
import com.cwj.onlineorder.entity.RestaurantEntity;
import com.cwj.onlineorder.model.MenuItemDto;
import com.cwj.onlineorder.model.RestaurantDto;
import com.cwj.onlineorder.repository.MenuItemRepository;
import com.cwj.onlineorder.repository.RestaurantRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 餐厅服务层。
 *
 * 负责处理餐厅浏览相关的业务逻辑：
 *   - 获取所有餐厅及其菜品列表（嵌套结构）
 *
 * 核心设计思路 — 减少 N+1 查询问题：
 *   如果按传统方式遍历每个餐厅去查询菜品，会产生 N+1 条 SQL（N = 餐厅数量）。
 *   本实现采用"一次性加载 + 内存分组"的策略：
 *     - 一次查询所有餐厅
 *     - 一次查询所有菜品
 *     - 在内存中按餐厅 ID 将菜品分组
 *   整个方法只需 2 条 SQL，无论餐厅数量多少。
 *
 * 性能考量：
 *   内存分组方案适合菜品数量有限的场景（如 < 10000 条）。
 *   若数据量极大，应改用数据库 JOIN 查询或 MyBatis 的嵌套查询。
 */
@Service
public class RestaurantService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    /**
     * 构造器注入两个 Repository。
     */
    public RestaurantService(
            RestaurantRepository restaurantRepository,
            MenuItemRepository menuItemRepository
    ) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
    }

    /**
     * 获取所有餐厅及其菜品的完整列表。
     *
     * 返回的数据结构是"嵌套"的：
     *   每个餐厅对象中直接包含其所有菜品，不需要前端再次请求。
     *
     * 返回示例：
     * [
     *   {
     *     "id": 1,
     *     "name": "Burger King",
     *     "address": "...",
     *     "phone": "...",
     *     "imageUrl": "...",
     *     "menuItems": [
     *       { "id": 1, "name": "Whopper", "price": 6.39, ... },
     *       { "id": 2, "name": "Chicken Fries", "price": 4.89, ... }
     *     ]
     *   },
     *   { "id": 2, "name": "SGD Tofu House", "menuItems": [...] },
     *   ...
     * ]
     *
     * 注意：
     *   - 如果某个餐厅没有任何菜品，menuItems 字段序列化为空数组 [] 而非 null
     *   - @Cacheable 注解已保留（不同于 CartService 的自调用场景，
     *     RestaurantService 中 getRestaurants() 是 Controller 直接调用的，
     *     会经过 Spring 代理，缓存能正常生效）
     *   - 缓存 TTL 在 application.yaml 中配置（expireAfterWrite=60s）
     *
     * @return 所有餐厅及其菜品的 DTO 列表
     */
    @Cacheable("restaurants")
    public List<RestaurantDto> getRestaurants() {
        // 第 1 步：查询所有餐厅（1 条 SQL：SELECT * FROM restaurants）。
        List<RestaurantEntity> restaurantEntities = restaurantRepository.findAll();

        // 第 2 步：查询所有菜品（1 条 SQL：SELECT * FROM menu_items）。
        List<MenuItemEntity> menuItemEntities = menuItemRepository.findAll();

        // 第 3 步：在内存中按餐厅 ID 构建 HashMap：restaurantId → [菜品DTO列表]。
        // 这样后续查找每个餐厅的菜品只需 O(1) 时间（HashMap 查找）。
        // 遍历每条菜品记录，按其 restaurantId 分配到对应的列表中。
        Map<Long, List<MenuItemDto>> groupedMenuItems = new HashMap<>();
        for (MenuItemEntity menuItemEntity : menuItemEntities) {
            // computeIfAbsent：key 不存在时创建空 ArrayList，存在时返回已有列表
            List<MenuItemDto> group = groupedMenuItems.computeIfAbsent(
                    menuItemEntity.restaurantId(),
                    k -> new ArrayList<>()
            );
            // 将菜品实体转换为 DTO（去掉 restaurantId 字段，减少冗余数据）
            group.add(new MenuItemDto(menuItemEntity));
        }

        // 第 4 步：组装最终的餐厅 DTO 列表。
        // 遍历每个餐厅，从 HashMap 中取出其菜品列表，组合成完整的 RestaurantDto。
        List<RestaurantDto> results = new ArrayList<>();
        for (RestaurantEntity restaurantEntity : restaurantEntities) {
            results.add(new RestaurantDto(
                    restaurantEntity,
                    groupedMenuItems.get(restaurantEntity.id())
            ));
        }
        return results;
    }
}
