package com.cwj.onlineorder.controller;

import com.cwj.onlineorder.model.OrderDto;
import com.cwj.onlineorder.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 订单控制器。
 * 所有接口需要认证。
 */
@RestController
@Tag(name = "订单", description = "订单查询与状态更新接口")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    public java.util.List<OrderDto> getOrders(@AuthenticationPrincipal User user) {
        return orderService.getOrdersByEmail(user.getUsername());
    }

    @PatchMapping("/orders/{orderId}/status")
    public OrderDto updateOrderStatus(
            @AuthenticationPrincipal User user,
            @PathVariable long orderId,
            @RequestBody Map<String, String> body
    ) {
        String status = body.get("status");
        return orderService.updateOrderStatus(user.getUsername(), orderId, status);
    }
}
