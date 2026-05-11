package com.cwj.onlineorder.exception;

/**
 * 菜品不存在异常。
 *
 * 当根据 ID 查找菜品但数据库中不存在时抛出。
 *
 * 映射规则：
 * - 由 GlobalExceptionHandler.handleMenuItemNotFound() 捕获
 * - 统一返回 HTTP 404 Not Found 响应
 *
 * @see GlobalExceptionHandler#handleMenuItemNotFound(MenuItemNotFoundException)
 */
public class MenuItemNotFoundException extends RuntimeException {

    public MenuItemNotFoundException(long id) {
        super("菜品不存在，ID: " + id);
    }

    public MenuItemNotFoundException(String message) {
        super(message);
    }
}
