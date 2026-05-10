package com.cwj.onlineorder.service;

import com.cwj.onlineorder.exception.MenuItemNotFoundException;
import com.cwj.onlineorder.entity.MenuItemEntity;
import com.cwj.onlineorder.repository.MenuItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;

    public MenuItemService(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    public List<MenuItemEntity> getMenuItemsByRestaurantId(long restaurantId) {
        return menuItemRepository.getByRestaurantId(restaurantId);
    }

    public MenuItemEntity getMenuItemById(long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new MenuItemNotFoundException(id));
    }
}
