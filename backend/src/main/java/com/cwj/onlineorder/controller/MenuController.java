package com.cwj.onlineorder.controller;

import com.cwj.onlineorder.model.ApiResult;
import com.cwj.onlineorder.model.MenuItemDto;
import com.cwj.onlineorder.model.RestaurantDto;
import com.cwj.onlineorder.service.MenuItemService;
import com.cwj.onlineorder.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 餐厅和菜品公开接口控制器。
 *
 * 所有接口无需认证即可访问，用于餐厅浏览和菜品查询。
 *
 * 公开接口说明：
 * - GET /restaurants/menu：获取所有餐厅及其菜品的完整嵌套列表（推荐）
 * - GET /restaurant/{id}/menu：获取指定餐厅的菜品列表（备选，保留兼容性）
 *
 * 两个接口返回的数据结构不同：
 * - /restaurants/menu 返回完整的 RestaurantDto[]，每个餐厅内嵌 MenuItemDto[]
 * - /restaurant/{id}/menu 仅返回该餐厅的 MenuItemDto[]
 *
 * 前端建议使用 /restaurants/menu 一次获取所有数据，减少请求次数。
 *
 * @see RestaurantService#getRestaurants()
 * @see MenuItemService#getMenuItemsByRestaurantId(long)
 */
@RestController
@Tag(name = "餐厅", description = "餐厅浏览和菜品查询接口")
public class MenuController {

    private final RestaurantService restaurantService;
    private final MenuItemService menuItemService;

    public MenuController(RestaurantService restaurantService, MenuItemService menuItemService) {
        this.restaurantService = restaurantService;
        this.menuItemService = menuItemService;
    }

    /**
     * 获取指定餐厅的菜品列表。
     *
     * 备选接口。推荐使用 GET /restaurants/menu 一次获取所有数据。
     *
     * @param restaurantId 餐厅 ID
     * @return 该餐厅的所有菜品
     */
    @GetMapping("/restaurant/{restaurantId}/menu")
    @Operation(summary = "获取指定餐厅的菜品列表")
    public ApiResult<List<MenuItemDto>> getMenuByRestaurant(@PathVariable("restaurantId") long restaurantId) {
        List<MenuItemDto> items = menuItemService.getMenuItemsByRestaurantId(restaurantId).stream()
                .map(MenuItemDto::new)
                .toList();
        return ApiResult.ok(items);
    }

    /**
     * 获取所有餐厅及其菜品的完整嵌套列表。
     *
     * 推荐的前端入口接口。一次请求返回所有餐厅和菜品，
     * 无需多次调用即可渲染完整的餐厅列表和菜单。
     *
     * @return 所有餐厅及其菜品的 DTO 列表
     */
    @GetMapping("/restaurants/menu")
    @Operation(summary = "获取所有餐厅及其菜品的完整嵌套列表")
    public ApiResult<List<RestaurantDto>> getMenuForAllRestaurants() {
        return ApiResult.ok(restaurantService.getRestaurants());
    }
}
