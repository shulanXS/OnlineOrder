package com.cwj.onlineorder.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 健康检查控制器。
 *
 * 提供应用健康状态接口，用于：
 * - K8s/容器编排探活（liveness / readiness probe）
 * - 负载均衡器健康检测
 * - 监控告警系统
 *
 * 此接口无需认证。
 */
@RestController
@RequestMapping
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now().toString(),
                "service", "OnlineOrder"
        ));
    }
}
