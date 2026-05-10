package com.cwj.onlineorder.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security UserDetailsService 实现。
 *
 * 负责根据用户名（邮箱）从数据库加载用户信息，
 * 供 JWT 认证过滤器在 Token 校验后加载完整用户详情。
 *
 * 实现方式：
 * - 通过 CustomerRepository 查询 customers 表
 * - 构造 Spring Security 标准的 UserDetails 对象
 * - 遵循 Spring Security 的 UserDetailsService 合约
 *
 * @see JwtAuthFilter 调用此服务加载用户
 * @see JwtTokenProvider 从 Token 中提取用户名后调用此服务
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final com.cwj.onlineorder.repository.CustomerRepository customerRepository;

    public CustomUserDetailsService(com.cwj.onlineorder.repository.CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * 根据用户名（邮箱）加载用户详情。
     *
     * @param username 用户邮箱（登录时传入的用户名）
     * @return Spring Security UserDetails 对象
     * @throws UsernameNotFoundException 用户不存在时抛出
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.cwj.onlineorder.entity.CustomerEntity customer = customerRepository.findByEmail(username.toLowerCase());
        if (customer == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(customer.email())
                .password(customer.password())
                .roles("USER")
                .disabled(!customer.enabled())
                .build();
    }
}
