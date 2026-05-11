package com.cwj.onlineorder.service;

import com.cwj.onlineorder.entity.CustomerEntity;
import com.cwj.onlineorder.entity.OrderDetailEntity;
import com.cwj.onlineorder.entity.OrderEntity;
import com.cwj.onlineorder.exception.ForbiddenException;
import com.cwj.onlineorder.model.OrderDto;
import com.cwj.onlineorder.repository.CustomerRepository;
import com.cwj.onlineorder.repository.OrderDetailRepository;
import com.cwj.onlineorder.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单服务层。
 *
 * 负责处理订单相关的业务逻辑：
 * - 查询用户订单（含明细）
 * - 更新订单状态（含状态机校验）
 *
 * 订单状态机：
 * - 正向流转：PENDING -> CONFIRMED -> PREPARING -> SHIPPING -> COMPLETED
 * - 终态（COMPLETED / CANCELLED）的订单不能再修改状态
 * - 任意非终态可以直接跳转到终态
 *
 * 权限控制：
 * - 用户只能查看和修改自己的订单
 * - 越权操作抛出 ForbiddenException -> 403 Forbidden
 */
@Service
public class OrderService {

    /** 合法的订单状态列表（用于输入校验） */
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
     *
     * 使用批量查询避免 N+1 问题：
     * 1. 1 条 SQL 查询订单列表（按创建时间倒序）
     * 2. 1 条 SQL 查询所有相关明细（IN 查询）
     * 3. 在内存中按 orderId 分组，组装最终的 DTO 列表
     *
     * @param email 顾客邮箱
     * @return 订单列表（无明细的订单返回空 details 列表）
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

        // 批量查询所有明细（1 条 SQL：WHERE order_id IN (?, ?, ...)）
        List<Long> orderIds = orders.stream().map(OrderEntity::id).toList();
        List<OrderDetailEntity> allDetails = orderDetailRepository.findAllByOrderIdIn(orderIds);

        // 在内存中按 orderId 分组（避免循环中逐个查询明细）
        Map<Long, List<OrderDetailEntity>> groupedDetails = allDetails.stream()
                .collect(Collectors.groupingBy(OrderDetailEntity::orderId));

        return orders.stream()
                .map(order -> new OrderDto(order, groupedDetails.getOrDefault(order.id(), List.of())))
                .toList();
    }

    /**
     * 更新订单状态。
     *
     * 状态机规则：
     * - 合法的正向流转：PENDING -> CONFIRMED -> PREPARING -> SHIPPING -> COMPLETED
     * - 终态（COMPLETED / CANCELLED）的订单不能再修改状态
     * - 任意非终态可以直接跳转到终态（COMPLETED 或 CANCELLED）
     *
     * 权限控制：
     * - 用户只能修改自己创建的订单
     * - 越权操作抛出 ForbiddenException（映射到 HTTP 403）
     *
     * @param email     当前登录用户的邮箱
     * @param orderId   订单 ID
     * @param newStatus 新状态
     * @return 更新后的订单 DTO（含最新明细）
     * @throws IllegalArgumentException 参数无效或状态流转错误（映射到 HTTP 400）
     * @throws ForbiddenException     用户无权修改此订单（映射到 HTTP 403）
     */
    @Transactional
    public OrderDto updateOrderStatus(String email, long orderId, String newStatus) {
        CustomerEntity customer = customerRepository.findByEmail(email);
        if (customer == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在，ID: " + orderId));

        // 权限校验：确保用户只能修改自己的订单
        if (!order.customerId().equals(customer.id())) {
            throw new ForbiddenException("无权修改此订单");
        }

        // 校验状态值是否合法（是否为已知状态）
        if (!VALID_STATUSES.contains(newStatus)) {
            throw new IllegalArgumentException("无效的订单状态: " + newStatus);
        }

        String currentStatus = order.status();

        // 已达终态的订单不能再修改
        if ("COMPLETED".equals(currentStatus) || "CANCELLED".equals(currentStatus)) {
            throw new IllegalArgumentException("已结束（" + currentStatus + "）的订单不能再修改状态");
        }

        // 跳转到终态：允许直接跳转，不做状态机校验
        if ("COMPLETED".equals(newStatus) || "CANCELLED".equals(newStatus)) {
            // 跳过状态机校验，直接更新
        } else {
            // 正向流转校验：检查状态是否按预期推进
            String nextStatus = switch (currentStatus) {
                case "PENDING"   -> "CONFIRMED";
                case "CONFIRMED" -> "PREPARING";
                case "PREPARING"  -> "SHIPPING";
                case "SHIPPING"   -> "COMPLETED";
                default          -> null;
            };
            if (!newStatus.equals(nextStatus)) {
                throw new IllegalArgumentException(
                        "状态流转错误：当前状态为 " + currentStatus + "，下一步应为 " + nextStatus);
            }
        }

        // 构建更新后的实体并持久化
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
