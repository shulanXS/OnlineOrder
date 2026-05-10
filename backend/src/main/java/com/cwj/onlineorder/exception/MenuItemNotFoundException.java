package com.cwj.onlineorder.exception;

/**
 * 自定义异常：表示请求的菜品在数据库中不存在。
 */
public class MenuItemNotFoundException extends RuntimeException {

    public MenuItemNotFoundException(long id) {
        super("菜品不存在，ID: " + id);
    }

    public MenuItemNotFoundException(String message) {
        super(message);
    }
}
