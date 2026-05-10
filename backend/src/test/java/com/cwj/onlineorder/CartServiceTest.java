package com.cwj.onlineorder;

import com.cwj.onlineorder.entity.CartEntity;
import com.cwj.onlineorder.entity.CartItemEntity;
import com.cwj.onlineorder.entity.MenuItemEntity;
import com.cwj.onlineorder.repository.*;
import com.cwj.onlineorder.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CartService 单元测试")
class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private MenuItemRepository menuItemRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private OrderDetailRepository orderDetailRepository;
    @Mock private JdbcTemplate jdbcTemplate;

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartService(
                cartRepository,
                customerRepository,
                menuItemRepository,
                cartItemRepository,
                orderRepository,
                orderDetailRepository,
                jdbcTemplate
        );
    }

    @Test
    @DisplayName("添加商品到空购物车应创建新的购物车商品行")
    void addMenuItemToCart_shouldCreateNewCartItem_whenCartIsEmpty() {
        String email = "test@example.com";
        long menuItemId = 10L;
        long customerId = 1L;

        var customer = new com.cwj.onlineorder.entity.CustomerEntity(
                customerId, email, "pass", true, "F", "L");
        CartEntity cart = new CartEntity(1L, customerId, BigDecimal.ZERO);
        MenuItemEntity menuItem = new MenuItemEntity(menuItemId, 1L, "Burger",
                "desc", BigDecimal.valueOf(10.0), "url");

        when(customerRepository.findByEmail(email)).thenReturn(customer);
        when(cartRepository.getByCustomerId(customerId)).thenReturn(cart);
        when(menuItemRepository.findById(menuItemId)).thenReturn(Optional.of(menuItem));
        when(cartItemRepository.findByCartIdAndMenuItemId(cart.id(), menuItemId)).thenReturn(null);
        when(cartItemRepository.getAllByCartId(anyLong())).thenReturn(List.of());

        cartService.addMenuItemToCart(email, menuItemId);

        verify(cartItemRepository).save(argThat((CartItemEntity item) ->
                item.menuItemId() == menuItemId &&
                item.cartId() == cart.id() &&
                item.quantity() == 1 &&
                item.price().compareTo(BigDecimal.valueOf(10.0)) == 0
        ));
        verify(cartRepository).updateTotalPrice(eq(cart.id()), eq(BigDecimal.valueOf(10.0)));
    }

    @Test
    @DisplayName("添加已存在的商品应增加数量")
    void addMenuItemToCart_shouldIncrementQuantity_whenItemExists() {
        String email = "test@example.com";
        long menuItemId = 10L;
        long customerId = 1L;

        var customer = new com.cwj.onlineorder.entity.CustomerEntity(
                customerId, email, "pass", true, "F", "L");
        CartEntity cart = new CartEntity(1L, customerId, BigDecimal.valueOf(10.0));
        MenuItemEntity menuItem = new MenuItemEntity(menuItemId, 1L, "Burger",
                "desc", BigDecimal.valueOf(10.0), "url");
        CartItemEntity existingItem = new CartItemEntity(5L, menuItemId,
                cart.id(), BigDecimal.valueOf(10.0), 1);

        when(customerRepository.findByEmail(email)).thenReturn(customer);
        when(cartRepository.getByCustomerId(customerId)).thenReturn(cart);
        when(menuItemRepository.findById(menuItemId)).thenReturn(Optional.of(menuItem));
        when(cartItemRepository.findByCartIdAndMenuItemId(cart.id(), menuItemId))
                .thenReturn(existingItem);
        when(cartItemRepository.getAllByCartId(anyLong())).thenReturn(List.of());

        cartService.addMenuItemToCart(email, menuItemId);

        verify(cartItemRepository).save(argThat((CartItemEntity item) ->
                item.id() == 5L &&
                item.quantity() == 2
        ));
        verify(cartRepository).updateTotalPrice(eq(cart.id()), eq(BigDecimal.valueOf(20.0)));
    }

    @Test
    @DisplayName("添加不存在的菜品应抛出异常")
    void addMenuItemToCart_shouldThrow_whenMenuItemNotFound() {
        String email = "test@example.com";
        long menuItemId = 999L;
        var customer = new com.cwj.onlineorder.entity.CustomerEntity(
                1L, email, "pass", true, "F", "L");
        when(customerRepository.findByEmail(email)).thenReturn(customer);
        when(menuItemRepository.findById(menuItemId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> cartService.addMenuItemToCart(email, menuItemId)
        );
        assertTrue(ex.getMessage().contains("不存在"));
    }

    @Test
    @DisplayName("获取空购物车应返回正确的默认结构")
    void getCart_shouldReturnEmptyCart_whenCustomerHasNoCart() {
        String email = "test@example.com";
        var customer = new com.cwj.onlineorder.entity.CustomerEntity(
                1L, email, "pass", true, "F", "L");
        when(customerRepository.findByEmail(email)).thenReturn(customer);
        when(cartRepository.getByCustomerId(customer.id())).thenReturn(null);
        when(cartRepository.save(any())).thenAnswer(inv -> {
            CartEntity c = inv.getArgument(0);
            return new CartEntity(1L, c.customerId(), c.totalPrice());
        });
        when(cartItemRepository.getAllByCartId(anyLong())).thenReturn(List.of());

        var cartDto = cartService.getCart(email);

        assertNotNull(cartDto);
        assertEquals(BigDecimal.ZERO, cartDto.totalPrice());
        assertTrue(cartDto.cartItems().isEmpty());
    }

    @Test
    @DisplayName("更新商品数量为零时应删除商品")
    void updateItemQuantity_shouldDelete_whenQuantityIsZero() {
        String email = "test@example.com";
        long menuItemId = 10L;
        var customer = new com.cwj.onlineorder.entity.CustomerEntity(
                1L, email, "pass", true, "F", "L");
        CartEntity cart = new CartEntity(1L, customer.id(), BigDecimal.valueOf(10.0));
        CartItemEntity item = new CartItemEntity(5L, menuItemId, cart.id(),
                BigDecimal.valueOf(10.0), 1);

        when(customerRepository.findByEmail(email)).thenReturn(customer);
        when(cartRepository.getByCustomerId(customer.id())).thenReturn(cart);
        when(cartItemRepository.findByCartIdAndMenuItemId(cart.id(), menuItemId))
                .thenReturn(item);
        when(cartItemRepository.getAllByCartId(anyLong())).thenReturn(List.of());

        cartService.updateItemQuantity(email, menuItemId, 0);

        verify(cartItemRepository).deleteById(anyLong());
        verify(cartRepository).updateTotalPrice(anyLong(), any(BigDecimal.class));
    }
}
