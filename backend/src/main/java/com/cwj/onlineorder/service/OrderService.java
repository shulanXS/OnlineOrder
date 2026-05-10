package com.cwj.onlineorder.service;

import com.cwj.onlineorder.entity.CustomerEntity;
import com.cwj.onlineorder.entity.OrderDetailEntity;
import com.cwj.onlineorder.entity.OrderEntity;
import com.cwj.onlineorder.model.OrderDto;
import com.cwj.onlineorder.repository.CustomerRepository;
import com.cwj.onlineorder.repository.OrderDetailRepository;
import com.cwj.onlineorder.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final List<String> VALID_STATUSES = List.of(
            "PENDING", "CONFIRMED", "PREPARING", "SHIPPING", "COMPLETED", "CANCELLED"
    );

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CustomerRepository customerRepository;

    public OrderService(
            OrderRepository orderRepository,
            OrderDetailRepository orderDetailRepository,
            CustomerRepository customerRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * 根据用户邮箱查询所有订单（含明细）。
     * 使用批量查询避免 N+1 问题：1 条 SQL 查订单 + 1 条 SQL 查所有明细。
     */
    public List<OrderDto> getOrdersByEmail(String email) {
        CustomerEntity customer = customerRepository.findByEmail(email);
        if (customer == null) {
            return List.of();
        }

        List<OrderEntity> orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(customer.id());
        if (orders.isEmpty()) {
            return List.of();
        }

        List<Long> orderIds = orders.stream().map(OrderEntity::id).toList();
        List<OrderDetailEntity> allDetails = orderDetailRepository.findAllByOrderIdIn(orderIds);

        Map<Long, List<OrderDetailEntity>> groupedDetails = allDetails.stream()
                .collect(Collectors.groupingBy(OrderDetailEntity::orderId));

        return orders.stream()
                .map(order -> new OrderDto(order, groupedDetails.getOrDefault(order.id(), List.of())))
                .toList();
    }

    /**
     * 更新订单状态。
     *
     * @param email     当前登录用户的邮箱
     * @param orderId   订单 ID
     * @param newStatus 新状态
     * @return 更新后的订单 DTO
     */
    @Transactional
    public OrderDto updateOrderStatus(String email, long orderId, String newStatus) {
        CustomerEntity customer = customerRepository.findByEmail(email);
        if (customer == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在，ID: " + orderId));

        if (!order.customerId().equals(customer.id())) {
            throw new IllegalArgumentException("无权修改此订单");
        }

        if (!VALID_STATUSES.contains(newStatus)) {
            throw new IllegalArgumentException("无效的订单状态: " + newStatus);
        }

        String currentStatus = order.status();

        if ("COMPLETED".equals(currentStatus) || "CANCELLED".equals(currentStatus)) {
            throw new IllegalArgumentException("已结束（" + currentStatus + "）的订单不能再修改状态");
        }

        if ("COMPLETED".equals(newStatus) || "CANCELLED".equals(newStatus)) {
            // 允许直接改为终态
        } else {
            // 正常状态流转：PENDING -> CONFIRMED -> PREPARING -> SHIPPING -> COMPLETED
            String nextStatus = switch (currentStatus) {
                case "PENDING" -> "CONFIRMED";
                case "CONFIRMED" -> "PREPARING";
                case "PREPARING" -> "SHIPPING";
                case "SHIPPING" -> "COMPLETED";
                default -> null;
            };
            if (!newStatus.equals(nextStatus)) {
                throw new IllegalArgumentException(
                        "状态流转错误：当前状态为 " + currentStatus + "，下一步应为 " + nextStatus);
            }
        }

        OrderEntity updated = new OrderEntity(
                order.id(),
                order.customerId(),
                newStatus,
                order.totalPrice(),
                order.createdAt(),
                java.time.LocalDateTime.now()
        );
        OrderEntity saved = orderRepository.save(updated);

        List<OrderDetailEntity> details = orderDetailRepository.findByOrderId(saved.id());
        return new OrderDto(saved, details);
    }
}
