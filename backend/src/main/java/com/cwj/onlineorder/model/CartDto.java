package com.cwj.onlineorder.model;

import com.cwj.onlineorder.entity.CartEntity;
import java.math.BigDecimal;
import java.util.List;

public record CartDto(
        Long id,
        BigDecimal totalPrice,
        List<CartItemDto> cartItems
) {
    public CartDto(CartEntity entity, List<CartItemDto> cartItems) {
        this(entity.id(), entity.totalPrice(), cartItems);
    }
}
