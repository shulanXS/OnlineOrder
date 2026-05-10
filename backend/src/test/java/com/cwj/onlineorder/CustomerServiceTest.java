package com.cwj.onlineorder;

import com.cwj.onlineorder.entity.CustomerEntity;
import com.cwj.onlineorder.repository.CustomerRepository;
import com.cwj.onlineorder.repository.CartRepository;
import com.cwj.onlineorder.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService 单元测试")
class CustomerServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(cartRepository, customerRepository, passwordEncoder);
    }

    @Test
    @DisplayName("注册成功时应保存用户并创建购物车")
    void signUp_shouldSaveCustomerAndCreateCart() {
        // Given
        String email = "test@example.com";
        String password = "password123";
        String firstName = "John";
        String lastName = "Doe";

        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(customerRepository.save(any(CustomerEntity.class))).thenAnswer(invocation -> {
            CustomerEntity entity = invocation.getArgument(0);
            return new CustomerEntity(1L, entity.email(), entity.password(),
                    entity.enabled(), entity.firstName(), entity.lastName());
        });

        // When
        customerService.signUp(email, password, firstName, lastName);

        // Then
        // 验证邮箱被小写化
        verify(customerRepository).save(argThat(entity ->
                entity.email().equals("test@example.com") &&
                entity.firstName().equals(firstName) &&
                entity.lastName().equals(lastName)
        ));
        // 验证购物车被创建
        verify(cartRepository).save(any());
    }

    @Test
    @DisplayName("注册时应将邮箱小写化")
    void signUp_shouldLowercaseEmail() {
        // Given
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(customerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        customerService.signUp("Test@Example.COM", "pass", "F", "L");

        // Then
        verify(customerRepository).save(argThat(entity ->
                entity.email().equals("test@example.com")
        ));
    }

    @Test
    @DisplayName("getCustomerByEmail 应返回小写化查询结果")
    void getCustomerByEmail_shouldLowercaseInput() {
        // Given
        CustomerEntity expected = new CustomerEntity(1L, "test@example.com",
                "pass", true, "F", "L");
        when(customerRepository.findByEmail("test@example.com")).thenReturn(expected);

        // When
        CustomerEntity result = customerService.getCustomerByEmail("TEST@EXAMPLE.COM");

        // Then
        assertEquals(expected, result);
        verify(customerRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("existsByEmail 应正确判断用户是否存在")
    void existsByEmail_shouldReturnTrue_whenUserExists() {
        when(customerRepository.findByEmail("test@example.com"))
                .thenReturn(new CustomerEntity(1L, "test@example.com", "pass", true, "F", "L"));

        assertTrue(customerService.existsByEmail("test@example.com"));
    }

    @Test
    @DisplayName("existsByEmail 用户不存在时应返回 false")
    void existsByEmail_shouldReturnFalse_whenUserNotExists() {
        when(customerRepository.findByEmail("test@example.com")).thenReturn(null);

        assertFalse(customerService.existsByEmail("test@example.com"));
    }
}
