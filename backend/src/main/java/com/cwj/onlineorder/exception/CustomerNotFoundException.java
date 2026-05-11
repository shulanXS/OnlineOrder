package com.cwj.onlineorder.exception;

/**
 * 顾客不存在异常。
 *
 * 当根据邮箱查找顾客但数据库中不存在时抛出。
 *
 * 映射规则：
 * - 由 GlobalExceptionHandler.handleCustomerNotFound() 捕获
 * - 统一返回 HTTP 404 Not Found 响应
 *
 * @see GlobalExceptionHandler#handleCustomerNotFound(CustomerNotFoundException)
 */
public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String email) {
        super("顾客不存在，邮箱: " + email);
    }

    public CustomerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
