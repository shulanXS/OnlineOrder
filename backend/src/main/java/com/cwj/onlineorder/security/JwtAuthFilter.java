package com.cwj.onlineorder.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter.
 *
 * 拦截每个 HTTP 请求，从请求头中提取并校验 JWT Token，
 * 校验成功后，将用户信息注入 Spring Security 上下文。
 *
 * 工作流程：
 * 1. 从请求头 Authorization 字段提取 Bearer Token
 * 2. 调用 JwtTokenProvider 校验 Token 有效性
 * 3. 从 Token 中提取用户名，调用 UserDetailsService 加载用户信息
 * 4. 构建 UsernamePasswordAuthenticationToken 并存入 SecurityContext
 *
 * 注意：
 * - 继承 OncePerRequestFilter，确保每个请求只执行一次
 * - 仅处理携带有效 Token 的请求；无 Token 时走匿名访问流程
 * - 过滤器执行顺序在 UsernamePasswordAuthenticationFilter 之前
 * - Token 解析失败时直接返回 401 JSON，不继续 filter chain
 *
 * @see JwtTokenProvider Token 工具类
 * @see AppConfig 过滤器链配置
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtTokenProvider tokenProvider, UserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String jwt = extractTokenFromRequest(request);

        if (StringUtils.hasText(jwt)) {
            try {
                if (tokenProvider.validateToken(jwt)) {
                    String username = tokenProvider.getUsernameFromToken(jwt);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    writeUnauthorizedResponse(response, "Token 无效或已过期");
                    return;
                }
            } catch (JwtException | IllegalArgumentException ex) {
                log.warn("JWT 验证失败: {}", ex.getMessage());
                writeUnauthorizedResponse(response, "Token 无效或已过期");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void writeUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"status\":401,\"error\":\"UNAUTHORIZED\",\"message\":\"" + message + "\"}"
        );
    }

    /**
     * 从 HTTP 请求头中提取 JWT Token。
     *
     * @param request HTTP 请求
     * @return Token 字符串（不含 Bearer 前缀），或 null
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(JwtTokenProvider.HEADER_STRING);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(JwtTokenProvider.TOKEN_PREFIX)) {
            return bearerToken.substring(JwtTokenProvider.TOKEN_PREFIX.length());
        }
        return null;
    }
}
