package com.cwj.onlineorder.service;

import com.cwj.onlineorder.entity.CartEntity;
import com.cwj.onlineorder.entity.CartItemEntity;
import com.cwj.onlineorder.entity.MenuItemEntity;
import com.cwj.onlineorder.entity.OrderDetailEntity;
import com.cwj.onlineorder.entity.OrderEntity;
import com.cwj.onlineorder.model.CartDto;
import com.cwj.onlineorder.model.CartItemDto;
import com.cwj.onlineorder.model.OrderDto;
import com.cwj.onlineorder.repository.CartItemRepository;
import com.cwj.onlineorder.repository.CartRepository;
import com.cwj.onlineorder.repository.CustomerRepository;
import com.cwj.onlineorder.repository.MenuItemRepository;
import com.cwj.onlineorder.repository.OrderDetailRepository;
import com.cwj.onlineorder.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 购物车服务层。
 *
 * 负责处理购物车的所有业务逻辑：
 * - 添加商品到购物车
 * - 获取购物车详情
 * - 修改商品数量
 * - 清空购物车（结账）
 *
 * 设计要点：
 * - 每次添加商品都重新计算购物车总价（基于快照价格，不随菜品调价变化）
 * - 同一菜品重复添加时只更新数量，不创建新行（通过 UNIQUE 约束保证）
 * - 结账时创建订单快照，记录购买时的菜品信息（名称、价格、图片），
 *   即使菜品后续被修改或删除，订单记录不受影响
 *
 * 并发安全：
 * - 所有修改操作均使用 @Transactional 确保原子性
 * - 结账操作使用 SELECT ... FOR UPDATE 悲观锁，防止并发结算
 */
@Service
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;
    private final CustomerRepository customerRepository;
    private final MenuItemRepository menuItemRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final JdbcTemplate jdbcTemplate;

    public CartService(
            CartRepository cartRepository,
            CustomerRepository customerRepository,
            MenuItemRepository menuItemRepository,
            CartItemRepository cartItemRepository,
            OrderRepository orderRepository,
            OrderDetailRepository orderDetailRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.cartRepository = cartRepository;
        this.customerRepository = customerRepository;
        this.menuItemRepository = menuItemRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 添加菜品到购物车。
     *
     * 业务规则：
     * - 如果该菜品尚未在购物车中：创建新行，quantity = 1
     * - 如果该菜品已在购物车中：已有行的 quantity += 1
     * - 购物车总价自动增加该菜品的当前单价
     *
     * @param email      顾客邮箱（登录用户名）
     * @param menuItemId 菜品 ID
     */
    @Transactional
    public CartDto addMenuItemToCart(String email, long menuItemId) {
        var customer = customerRepository.findByEmail(email.toLowerCase());
        if (customer == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        long customerId = customer.id();

        CartEntity cart = cartRepository.getByCustomerId(customerId);
        if (cart == null) {
            cart = new CartEntity(null, customerId, BigDecimal.ZERO);
            cart = cartRepository.save(cart);
        }

        MenuItemEntity menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new IllegalArgumentException("菜品不存在，ID: " + menuItemId));

        CartItemEntity existingItem = cartItemRepository.findByCartIdAndMenuItemId(
                cart.id(), menuItem.id()
        );

        Long cartItemId;
        int quantity;

        if (existingItem == null) {
            cartItemId = null;
            quantity = 1;
        } else {
            cartItemId = existingItem.id();
            quantity = existingItem.quantity() + 1;
        }

        BigDecimal price = menuItem.price();
        CartItemEntity newItem = new CartItemEntity(
                cartItemId,
                menuItemId,
                cart.id(),
                price,
                quantity
        );
        cartItemRepository.save(newItem);

        cartRepository.updateTotalPrice(cart.id(),
                cart.totalPrice().add(price));

        return getCart(email);
    }

    /**
     * 获取购物车详情。
     *
     * @param email 顾客邮箱
     * @return 购物车 DTO，包含所有商品行及菜品信息
     */
    public CartDto getCart(String email) {
        var customer = customerRepository.findByEmail(email.toLowerCase());
        if (customer == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        long customerId = customer.id();

        CartEntity cart = cartRepository.getByCustomerId(customerId);
        if (cart == null) {
            CartEntity newCart = new CartEntity(null, customerId, BigDecimal.ZERO);
            cart = cartRepository.save(newCart);
        }

        List<CartItemEntity> cartItems = cartItemRepository.getAllByCartId(cart.id());
        List<CartItemDto> cartItemDtos = enrichCartItems(cartItems);

        return new CartDto(cart, cartItemDtos);
    }

    /**
     * 结账：生成订单记录并清空购物车。
     *
     * 事务边界：整个方法在一个事务中执行，确保：
     * 1. 订单 + 订单明细保存成功
     * 2. 购物车清空
     * 任意一步失败则整体回滚。
     *
     * 并发安全：使用 SELECT ... FOR UPDATE 对购物车行加锁，
     * 确保同一时刻只有一个请求能对同一购物车执行结账操作。
     *
     * @param email 顾客邮箱
     * @return 创建的订单 DTO
     * @throws IllegalArgumentException 购物车为空时抛出
     */
    @Transactional
    public OrderDto clearCart(String email) {
        var customer = customerRepository.findByEmail(email.toLowerCase());
        if (customer == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        long customerId = customer.id();

        CartEntity cartEntity = jdbcTemplate.queryForObject(
                "SELECT id, customer_id, total_price FROM carts WHERE customer_id = ? FOR UPDATE",
                (rs, rowNum) -> new CartEntity(
                        rs.getLong("id"),
                        rs.getLong("customer_id"),
                        rs.getBigDecimal("total_price")
                ),
                customerId
        );

        if (cartEntity == null) {
            throw new IllegalArgumentException("购物车不存在");
        }

        if (cartEntity.totalPrice().compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("购物车为空，无法结账");
        }

        List<CartItemEntity> cartItems = cartItemRepository.getAllByCartId(cartEntity.id());

        LocalDateTime now = LocalDateTime.now();
        OrderEntity order = new OrderEntity(
                null,
                customerId,
                "PENDING",
                cartEntity.totalPrice(),
                now,
                now
        );
        OrderEntity savedOrder = orderRepository.save(order);

        if (!cartItems.isEmpty()) {
            Map<Long, MenuItemEntity> menuItemMap = buildMenuItemMap(cartItems);
            List<OrderDetailEntity> orderDetails = new ArrayList<>();

            for (CartItemEntity item : cartItems) {
                MenuItemEntity menuItem = menuItemMap.get(item.menuItemId());
                if (menuItem != null) {
                    orderDetails.add(new OrderDetailEntity(
                            null,
                            savedOrder.id(),
                            item.menuItemId(),
                            menuItem.name(),
                            menuItem.description(),
                            menuItem.imageUrl(),
                            item.price(),
                            item.quantity()
                    ));
                } else {
                    log.warn("菜品 ID={} 已被删除，跳过订单明细快照，购物车商品行 ID={}",
                            item.menuItemId(), item.id());
                }
            }
            orderDetailRepository.saveAll(orderDetails);
        }

        cartItemRepository.deleteByCartId(cartEntity.id());
        cartRepository.updateTotalPrice(cartEntity.id(), BigDecimal.ZERO);

        List<OrderDetailEntity> details = orderDetailRepository.findByOrderId(savedOrder.id());
        return new OrderDto(savedOrder, details);
    }

    /**
     * 更新购物车中指定商品的数量。
     *
     * 业务规则：
     * - quantity <= 0：删除该商品行
     * - quantity > 0：更新为指定数量
     *
     * @param email       顾客邮箱
     * @param menuItemId  菜品 ID
     * @param newQuantity 新数量
     */
    @Transactional
    public CartDto updateItemQuantity(String email, Long menuItemId, int newQuantity) {
        var customer = customerRepository.findByEmail(email.toLowerCase());
        if (customer == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        long customerId = customer.id();

        CartEntity cart = cartRepository.getByCustomerId(customerId);
        if (cart == null) {
            throw new IllegalArgumentException("购物车不存在");
        }

        CartItemEntity item = cartItemRepository.findByCartIdAndMenuItemId(cart.id(), menuItemId);
        if (item == null) {
            throw new IllegalArgumentException("购物车中不存在该商品");
        }

        if (newQuantity <= 0) {
            BigDecimal removedTotal = item.price().multiply(BigDecimal.valueOf(item.quantity()));
            cartItemRepository.deleteById(item.id());
            cartRepository.updateTotalPrice(cart.id(), cart.totalPrice().subtract(removedTotal));
        } else {
            BigDecimal priceDiff = item.price().multiply(BigDecimal.valueOf(newQuantity - item.quantity()));
            CartItemEntity updatedItem = new CartItemEntity(
                    item.id(), item.menuItemId(), cart.id(), item.price(), newQuantity
            );
            cartItemRepository.save(updatedItem);
            cartRepository.updateTotalPrice(cart.id(), cart.totalPrice().add(priceDiff));
        }

        return getCart(email);
    }

    /**
     * 为购物车商品行列表补充菜品详细信息（名称、描述、图片）。
     * 使用批量查询避免 N+1 问题。
     */
    private List<CartItemDto> enrichCartItems(List<CartItemEntity> cartItems) {
        if (cartItems.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, MenuItemEntity> menuItemMap = buildMenuItemMap(cartItems);

        List<CartItemDto> result = new ArrayList<>();
        for (CartItemEntity cartItem : cartItems) {
            MenuItemEntity menuItem = menuItemMap.get(cartItem.menuItemId());
            if (menuItem != null) {
                result.add(new CartItemDto(cartItem, menuItem));
            } else {
                log.warn("菜品 ID={} 已被删除，但仍在购物车商品行 ID={} 中，将被忽略",
                        cartItem.menuItemId(), cartItem.id());
            }
        }
        return result;
    }

    /**
     * 构建菜品 ID 到菜品实体的映射。批量查询避免 N+1。
     */
    private Map<Long, MenuItemEntity> buildMenuItemMap(List<CartItemEntity> cartItems) {
        List<Long> menuItemIds = cartItems.stream()
                .map(CartItemEntity::menuItemId)
                .toList();
        Map<Long, MenuItemEntity> map = new HashMap<>();
        for (MenuItemEntity item : menuItemRepository.findAllById(menuItemIds)) {
            map.put(item.id(), item);
        }
        return map;
    }
}
