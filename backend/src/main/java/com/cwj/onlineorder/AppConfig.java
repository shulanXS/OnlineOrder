package com.cwj.onlineorder;

import com.cwj.onlineorder.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 全局配置。
 *
 * 核心设计：
 * - 使用 JWT Token 进行无状态认证（SessionCreationPolicy.STATELESS）
 * - JWT 过滤器（JwtAuthFilter）在 UsernamePasswordAuthenticationFilter 之前执行
 * - CORS 配置允许前端跨域请求，来源通过环境变量配置
 *
 * 认证流程：
 * 1. JwtAuthFilter 拦截请求，解析 Authorization Header 中的 Bearer Token
 * 2. Token 有效则将用户信息注入 SecurityContext
 * 3. Spring Security 根据 SecurityContext 判断请求是否已认证
 *
 * 过滤器链顺序（重要）：
 * - CORS Filter -> JwtAuthFilter -> UsernamePasswordAuthenticationFilter -> ...
 * - JwtAuthFilter 在 UsernamePasswordAuthenticationFilter 之前执行，
 *   因为表单登录只在 POST /auth/login 时使用，其他请求都需要 JWT 认证
 *
 * 路由权限设计：
 * - 公开接口：登录 (/auth/login)、注册 (/auth/register)、餐厅浏览 (/restaurants/**)
 * - 认证接口：购物车、订单、用户信息（除上述以外的所有接口）
 *
 * 公开路径与 JwtAuthFilter.shouldNotFilter() 保持同步：
 * - JwtAuthFilter.PUBLIC_PREFIXES 定义了无需 JWT 校验的路径前缀
 * - filterChain 中的 permitAll() 定义了允许匿名访问的路径
 * - 两处需同时维护，确保一致性
 *
 * @see JwtAuthFilter
 */
@Configuration
public class AppConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final String corsAllowedOrigins;

    public AppConfig(
            JwtAuthFilter jwtAuthFilter,
            @Value("${cors.allowed-origins}") String corsAllowedOrigins
    ) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.corsAllowedOrigins = corsAllowedOrigins;
    }

    /**
     * 安全过滤器链配置。
     *
     * @param http HttpSecurity 对象
     * @return 配置好的 SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // REST API 场景下不需要 CSRF（使用 JWT Token 而非 Cookie）
                .csrf(AbstractHttpConfigurer::disable)
                // CORS 配置：允许前端跨域
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 无状态会话：Spring Security 不创建或使用 HTTP Session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 路由权限配置
                .authorizeHttpRequests(auth -> auth
                        // 静态资源：CSS、JS、图片等
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico", "/error")
                                .permitAll()
                        // 前端入口文件
                        .requestMatchers(HttpMethod.GET, "/", "/index.html", "/*.json", "/*.png", "/static/**")
                                .permitAll()
                        // 认证接口：登录、注册
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/register")
                                .permitAll()
                        // 餐厅浏览：公开接口（支持嵌套路由如 /restaurants/menu 和 /restaurant/{id}/menu）
                        .requestMatchers(HttpMethod.GET, "/restaurants/**", "/restaurant/**")
                                .permitAll()
                        // OpenAPI / Swagger 文档
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html")
                                .permitAll()
                        // Actuator 健康检查
                        .requestMatchers("/actuator/health", "/actuator/info")
                                .permitAll()
                        // 所有其他请求需要认证
                        .anyRequest().authenticated()
                )
                // 未认证时返回 401 JSON 响应（而非重定向到登录页）
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                // 将 JWT 过滤器注册到过滤器链，在表单登录过滤器之前执行
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS 跨域配置。
     *
     * 允许配置的前端跨域请求，支持 Cookie 和所有常见 HTTP 方法。
     * 来源通过 cors.allowed-origins 环境变量配置（支持逗号分隔多源）。
     *
     * @return CORS 配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 支持逗号分隔的多个 CORS 来源
        configuration.setAllowedOrigins(
                List.of(corsAllowedOrigins.split(",")).stream()
                        .map(String::trim)
                        .toList()
        );
        configuration.setAllowCredentials(true);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Content-Type", "Authorization", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 密码编码器。
     *
     * 使用 BCrypt 哈希算法：
     * - 每次编码结果不同（自带随机盐值）
     * - 不可逆
     * - 内置强度参数（默认 10 轮）
     *
     * @return PasswordEncoder 实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager Bean。
     *
     * 供 AuthController 注入，用于执行用户名密码认证。
     * 由 Spring Security 自动配置类从 AuthenticationConfiguration 中构建。
     *
     * @param config AuthenticationConfiguration
     * @return AuthenticationManager
     * @throws Exception 配置异常
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
