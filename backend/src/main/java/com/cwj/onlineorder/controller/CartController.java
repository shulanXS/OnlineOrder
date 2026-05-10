package com.cwj.onlineorder.controller;

import com.cwj.onlineorder.model.CartDto;
import com.cwj.onlineorder.model.OrderDto;
import com.cwj.onlineorder.service.CartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 购物车控制器。
 * 所有接口需要认证。
 */
@RestController
@RequestMapping("/cart")
@Tag(name = "购物车", description = "购物车管理和结账接口")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartDto getCart(@AuthenticationPrincipal User user) {
        return cartService.getCart(user.getUsername());
    }

    @PostMapping
    public CartDto addToCart(@AuthenticationPrincipal User user, @RequestBody Map<String, Long> body) {
        return cartService.addMenuItemToCart(user.getUsername(), body.get("menuId"));
    }

    @PostMapping("/items/{menuItemId}")
    public CartDto updateItemQuantity(
            @AuthenticationPrincipal User user,
            @PathVariable long menuItemId,
            @RequestBody Map<String, Integer> body
    ) {
        return cartService.updateItemQuantity(user.getUsername(), menuItemId, body.get("quantity"));
    }

    @PostMapping("/checkout")
    public OrderDto checkout(@AuthenticationPrincipal User user) {
        return cartService.clearCart(user.getUsername());
    }
}
