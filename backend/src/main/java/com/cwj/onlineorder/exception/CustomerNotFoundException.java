package com.cwj.onlineorder.exception;

/**
 * 自定义异常：表示请求的顾客在数据库中不存在。
 */
public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String email) {
        super("顾客不存在，邮箱: " + email);
    }

    public CustomerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
