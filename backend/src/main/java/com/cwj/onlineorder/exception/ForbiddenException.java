package com.cwj.onlineorder.exception;

/**
 * 禁止访问异常（Forbidden Exception）。
 *
 * 当用户尝试访问或操作其权限范围之外的资源时抛出。
 *
 * 映射规则：
 * - 由 GlobalExceptionHandler.handleForbidden() 捕获
 * - 统一返回 HTTP 403 Forbidden 响应
 *
 * 使用场景：
 * - 用户尝试修改他人的订单（OrderService.updateOrderStatus）
 *
 * @see GlobalExceptionHandler#handleForbidden(ForbiddenException)
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
