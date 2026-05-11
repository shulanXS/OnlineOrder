package com.cwj.onlineorder.service;

import com.cwj.onlineorder.entity.CartEntity;
import com.cwj.onlineorder.entity.CustomerEntity;
import com.cwj.onlineorder.repository.CartRepository;
import com.cwj.onlineorder.repository.CustomerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 顾客服务层。
 *
 * 负责处理用户注册、个人信息查询等顾客相关业务逻辑。
 *
 * 设计要点：
 * - 用户注册时自动创建购物车（确保每个用户有且仅有一个购物车）
 * - 使用 BCrypt 密码编码，不可逆存储
 * - 邮箱统一小写化处理，避免重复注册
 */
@Service
public class CustomerService {

    private final CartRepository cartRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(
            CartRepository cartRepository,
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.cartRepository = cartRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 用户注册。
     *
     * 执行步骤：
     * 1. 邮箱小写化（统一存储格式）
     * 2. 密码 BCrypt 哈希（不可逆加密）
     * 3. 保存 customers 表记录
     * 4. 创建空购物车
     *
     * 注意：此方法不返回 Token。Token 由 AuthController 在注册成功后生成并返回，
     * 这样前端注册后无需二次登录。
     *
     * @param email     邮箱地址（登录用户名）
     * @param password  明文密码（将经过 BCrypt 哈希）
     * @param firstName 名
     * @param lastName  姓
     */
    @Transactional
    public void signUp(String email, String password, String firstName, String lastName) {
        email = email.toLowerCase();

        String encodedPassword = passwordEncoder.encode(password);

        CustomerEntity customer = new CustomerEntity(
                null,
                email,
                encodedPassword,
                true,
                firstName,
                lastName
        );
        customerRepository.save(customer);

        // 为新用户创建购物车
        CartEntity cart = new CartEntity(null, customer.id(), BigDecimal.ZERO);
        cartRepository.save(cart);
    }

    /**
     * 根据邮箱查询顾客信息。
     *
     * @param email 顾客邮箱
     * @return 顾客实体，如果不存在返回 null
     */
    public CustomerEntity getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email.toLowerCase());
    }

    /**
     * 根据邮箱检查用户是否存在。
     *
     * @param email 顾客邮箱
     * @return true 表示用户已存在
     */
    public boolean existsByEmail(String email) {
        return customerRepository.findByEmail(email.toLowerCase()) != null;
    }
}
