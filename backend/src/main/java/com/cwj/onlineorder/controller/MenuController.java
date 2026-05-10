package com.cwj.onlineorder.controller;

import com.cwj.onlineorder.model.MenuItemDto;
import com.cwj.onlineorder.model.RestaurantDto;
import com.cwj.onlineorder.service.MenuItemService;
import com.cwj.onlineorder.service.RestaurantService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 餐厅和菜品控制器。
 * 所有接口公开，无需认证。
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

    @GetMapping("/restaurant/{restaurantId}/menu")
    public List<MenuItemDto> getMenuByRestaurant(@PathVariable("restaurantId") long restaurantId) {
        return menuItemService.getMenuItemsByRestaurantId(restaurantId).stream()
                .map(MenuItemDto::new)
                .toList();
    }

    @GetMapping("/restaurants/menu")
    public List<RestaurantDto> getMenuForAllRestaurants() {
        return restaurantService.getRestaurants();
    }
}
