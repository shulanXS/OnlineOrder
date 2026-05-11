package com.cwj.onlineorder.controller;

import com.cwj.onlineorder.dto.UpdateStatusRequest;
import com.cwj.onlineorder.model.ApiResult;
import com.cwj.onlineorder.model.OrderDto;
import com.cwj.onlineorder.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器。
 * 所有接口需要认证。
 */
@RestController
@Validated
@Tag(name = "订单", description = "订单查询与状态更新接口")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    public ApiResult<List<OrderDto>> getOrders(@AuthenticationPrincipal User user) {
        return ApiResult.ok(orderService.getOrdersByEmail(user.getUsername()));
    }

    @PatchMapping("/orders/{orderId}/status")
    public ApiResult<OrderDto> updateOrderStatus(
            @AuthenticationPrincipal User user,
            @PathVariable long orderId,
            @Valid @RequestBody UpdateStatusRequest request
    ) {
        return ApiResult.ok(orderService.updateOrderStatus(user.getUsername(), orderId, request.status()));
    }
}
