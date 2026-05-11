package com.cwj.onlineorder.controller;

import com.cwj.onlineorder.exception.CustomerNotFoundException;
import com.cwj.onlineorder.model.ApiResult;
import com.cwj.onlineorder.model.AuthResponse;
import com.cwj.onlineorder.model.CustomerDto;
import com.cwj.onlineorder.model.LoginRequest;
import com.cwj.onlineorder.model.RegisterBody;
import com.cwj.onlineorder.security.JwtTokenProvider;
import com.cwj.onlineorder.service.CustomerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器。
 *
 * 提供用户登录、注册和认证状态查询接口。
 *
 * 权限设计：
 * - 登录 POST /auth/login：公开
 * - 注册 POST /auth/register：公开
 * - 当前用户 GET /auth/me：需要认证
 *
 * 认证流程：
 * 1. 用户提交邮箱 + 密码
 * 2. AuthenticationManager 验证凭证（通过 CustomUserDetailsService 加载用户）
 * 3. 验证通过后生成 JWT Token 并返回给前端
 * 4. 前端后续请求携带 Token，JwtAuthFilter 自动完成认证
 *
 * @see com.cwj.onlineorder.security.JwtAuthFilter
 * @see com.cwj.onlineorder.security.CustomUserDetailsService
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "认证", description = "用户登录、注册、认证状态接口")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final CustomerService customerService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtTokenProvider tokenProvider,
            CustomerService customerService
    ) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.customerService = customerService;
    }

    /**
     * 获取当前登录用户信息。
     * 需要认证（携带有效 JWT Token）。
     *
     * @param user 从 SecurityContext 注入的当前登录用户
     * @return 当前用户信息
     * @throws CustomerNotFoundException 用户不存在时抛出（映射到 404）
     */
    @GetMapping("/me")
    public ApiResult<CustomerDto> getCurrentUser(@AuthenticationPrincipal User user) {
        var customer = customerService.getCustomerByEmail(user.getUsername());
        if (customer == null) {
            throw new CustomerNotFoundException("用户不存在: " + user.getUsername());
        }
        CustomerDto dto = new CustomerDto(
                customer.id(),
                customer.email(),
                customer.firstName(),
                customer.lastName()
        );
        return ApiResult.ok(dto);
    }

    /**
     * 用户登录。
     *
     * 执行流程：
     * 1. 邮箱小写化（与注册时的存储格式一致）
     * 2. AuthenticationManager.authenticate() 验证用户名密码
     * 3. 验证通过后生成 JWT Token
     * 4. 返回 Token 响应
     *
     * @param request 登录请求体（邮箱 + 密码）
     * @return 成功返回 200 + Token；失败返回 401
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResult<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            // 邮箱小写化与注册时保持一致
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email().toLowerCase(),
                            request.password()
                    )
            );
            String token = tokenProvider.generateToken(authentication);
            AuthResponse body = new AuthResponse(
                    token,
                    "Bearer",
                    tokenProvider.getExpirationSeconds(token)
            );
            return ResponseEntity.ok(ApiResult.ok(body));
        } catch (BadCredentialsException ex) {
            // BadCredentialsException 是 AuthenticationException 的子类，
            // 这里先捕获是为了返回更具体的错误信息 "用户名或密码错误"。
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResult.fail(
                            HttpStatus.UNAUTHORIZED.value(),
                            "UNAUTHORIZED",
                            "用户名或密码错误"
                    ));
        } catch (AuthenticationException ex) {
            // 其他认证异常（理论上只有 BadCredentialsException 会在这里触发，
            // 因为 loadUserByUsername 不会抛出 UsernameNotFoundException，
            // 它只返回 null 再到 AuthenticationManager 抛 BadCredentialsException）。
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResult.fail(
                            HttpStatus.UNAUTHORIZED.value(),
                            "UNAUTHORIZED",
                            "认证失败"
                    ));
        }
    }

    /**
     * 用户注册。
     *
     * 执行流程：
     * 1. CustomerService.signUp() 创建用户并自动创建购物车
     * 2. 注册成功后立即执行登录认证，无需用户二次登录
     * 3. 返回 JWT Token
     *
     * 注意：注册时的邮箱已经在 CustomerService 中做了小写化处理，
     * 因此 authenticate() 时也必须传入小写化的邮箱。
     *
     * @param body 注册信息（邮箱 + 密码 + 姓名）
     * @return 成功返回 201 + Token
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResult<AuthResponse>> register(@Valid @RequestBody RegisterBody body) {
        customerService.signUp(
                body.email(),
                body.password(),
                body.firstName(),
                body.lastName()
        );
        // 注册成功后立即登录，无需二次认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        body.email().toLowerCase(),
                        body.password()
                )
        );
        String token = tokenProvider.generateToken(authentication);
        AuthResponse authResponse = new AuthResponse(
                token,
                "Bearer",
                tokenProvider.getExpirationSeconds(token)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.ok(authResponse));
    }
}
