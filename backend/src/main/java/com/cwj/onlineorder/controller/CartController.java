package com.cwj.onlineorder.controller;

import com.cwj.onlineorder.dto.AddToCartRequest;
import com.cwj.onlineorder.dto.UpdateQuantityRequest;
import com.cwj.onlineorder.model.ApiResult;
import com.cwj.onlineorder.model.CartDto;
import com.cwj.onlineorder.model.OrderDto;
import com.cwj.onlineorder.service.CartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 购物车控制器。
 * 所有接口需要认证。
 */
@RestController
@RequestMapping("/cart")
@Validated
@Tag(name = "购物车", description = "购物车管理和结账接口")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ApiResult<CartDto> getCart(@AuthenticationPrincipal User user) {
        return ApiResult.ok(cartService.getCart(user.getUsername()));
    }

    @PostMapping
    public ApiResult<CartDto> addToCart(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AddToCartRequest request
    ) {
        return ApiResult.ok(cartService.addMenuItemToCart(user.getUsername(), request.menuId()));
    }

    @PostMapping("/items/{menuItemId}")
    public ApiResult<CartDto> updateItemQuantity(
            @AuthenticationPrincipal User user,
            @PathVariable long menuItemId,
            @Valid @RequestBody UpdateQuantityRequest request
    ) {
        return ApiResult.ok(cartService.updateItemQuantity(user.getUsername(), menuItemId, request.quantity()));
    }

    @PostMapping("/checkout")
    public ApiResult<OrderDto> checkout(@AuthenticationPrincipal User user) {
        return ApiResult.ok(cartService.clearCart(user.getUsername()));
    }
}
