package com.cwj.onlineorder;

import com.cwj.onlineorder.entity.CustomerEntity;
import com.cwj.onlineorder.entity.OrderDetailEntity;
import com.cwj.onlineorder.model.OrderDto;
import com.cwj.onlineorder.model.OrderDetailDto;
import com.cwj.onlineorder.repository.CustomerRepository;
import com.cwj.onlineorder.repository.OrderDetailRepository;
import com.cwj.onlineorder.repository.OrderRepository;
import com.cwj.onlineorder.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService 单元测试")
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderDetailRepository orderDetailRepository;
    @Mock private CustomerRepository customerRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, orderDetailRepository, customerRepository);
    }

    @Test
    @DisplayName("根据邮箱查询订单应返回倒序排列的订单列表")
    void getOrdersByEmail_shouldReturnOrdersDescending() {
        String email = "test@example.com";
        var customer = new CustomerEntity(1L, email, "pass", true, "F", "L");
        var order1 = new com.cwj.onlineorder.entity.OrderEntity(
                1L, customer.id(), "PENDING", BigDecimal.valueOf(50.0),
                LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(2));
        var order2 = new com.cwj.onlineorder.entity.OrderEntity(
                2L, customer.id(), "COMPLETED", BigDecimal.valueOf(30.0),
                LocalDateTime.now(), LocalDateTime.now());

        when(customerRepository.findByEmail(email)).thenReturn(customer);
        when(orderRepository.findByCustomerIdOrderByCreatedAtDesc(customer.id()))
                .thenReturn(List.of(order2, order1));
        when(orderDetailRepository.findAllByOrderIdIn(any()))
                .thenReturn(List.of());

        List<OrderDto> results = orderService.getOrdersByEmail(email);

        assertEquals(2, results.size());
        assertEquals(2L, results.get(0).id());
        assertEquals(1L, results.get(1).id());
    }

    @Test
    @DisplayName("用户不存在时应返回空列表")
    void getOrdersByEmail_shouldReturnEmptyList_whenUserNotFound() {
        when(customerRepository.findByEmail("unknown@example.com")).thenReturn(null);

        List<OrderDto> results = orderService.getOrdersByEmail("unknown@example.com");

        assertTrue(results.isEmpty());
        verify(orderRepository, never()).findByCustomerIdOrderByCreatedAtDesc(any());
    }

    @Test
    @DisplayName("订单明细应被正确映射到 DTO")
    void getOrdersByEmail_shouldMapDetailsCorrectly() {
        String email = "test@example.com";
        var customer = new CustomerEntity(1L, email, "pass", true, "F", "L");
        var order = new com.cwj.onlineorder.entity.OrderEntity(
                1L, customer.id(), "PENDING", BigDecimal.valueOf(20.0),
                LocalDateTime.now(), LocalDateTime.now());

        var detailEntity = new OrderDetailEntity(
                1L, order.id(), 10L, "Burger", "Delicious", "url",
                BigDecimal.valueOf(10.0), 2
        );

        when(customerRepository.findByEmail(email)).thenReturn(customer);
        when(orderRepository.findByCustomerIdOrderByCreatedAtDesc(customer.id()))
                .thenReturn(List.of(order));
        when(orderDetailRepository.findAllByOrderIdIn(any()))
                .thenReturn(List.of(detailEntity));

        List<OrderDto> results = orderService.getOrdersByEmail(email);

        assertEquals(1, results.size());
        OrderDto dto = results.get(0);
        assertEquals(1, dto.details().size());
        OrderDetailDto detailDto = dto.details().get(0);
        assertEquals("Burger", detailDto.menuItemName());
        assertEquals(BigDecimal.valueOf(10.0), detailDto.price());
        assertEquals(2, detailDto.quantity());
    }
}
