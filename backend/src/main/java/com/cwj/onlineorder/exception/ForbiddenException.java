package com.cwj.onlineorder.exception;

/**
 * 禁止访问异常。
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

    /**
     * 根据错误描述构造异常。
     *
     * @param message 错误描述信息
     */
    public ForbiddenException(String message) {
        super(message);
    }
}
