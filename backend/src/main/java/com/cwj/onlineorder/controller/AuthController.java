package com.cwj.onlineorder.controller;

import com.cwj.onlineorder.exception.CustomerNotFoundException;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器。
 * 登录和注册接口公开，/auth/me 需要认证。
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
     */
    @GetMapping("/me")
    public CustomerDto getCurrentUser(@AuthenticationPrincipal User user) {
        var customer = customerService.getCustomerByEmail(user.getUsername());
        if (customer == null) {
            throw new CustomerNotFoundException("用户不存在: " + user.getUsername());
        }
        return new CustomerDto(
                customer.id(),
                customer.email(),
                customer.firstName(),
                customer.lastName()
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username().toLowerCase(),
                            request.password()
                    )
            );
            String token = tokenProvider.generateToken(authentication);
            return ResponseEntity.ok(new AuthResponse(token, "Bearer", tokenProvider.getExpirationSeconds(token)));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "status", 401,
                            "error", "UNAUTHORIZED",
                            "message", "用户名或密码错误"
                    ));
        }
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterBody body) {
        customerService.signUp(body.email(), body.password(), body.firstName(), body.lastName());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(body.email().toLowerCase(), body.password())
        );
        String token = tokenProvider.generateToken(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, "Bearer", tokenProvider.getExpirationSeconds(token)));
    }
}
