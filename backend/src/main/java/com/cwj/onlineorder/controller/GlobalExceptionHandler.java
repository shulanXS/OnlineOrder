package com.cwj.onlineorder.controller;

import com.cwj.onlineorder.exception.CustomerNotFoundException;
import com.cwj.onlineorder.exception.MenuItemNotFoundException;
import com.cwj.onlineorder.model.ApiResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 * 统一返回 ApiResult 结构的错误响应。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<ApiResult<Void>> fail(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(ApiResult.fail(status.value(), code, message));
    }

    @ExceptionHandler(MenuItemNotFoundException.class)
    public ResponseEntity<ApiResult<Void>> handleMenuItemNotFound(MenuItemNotFoundException ex) {
        return fail(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ApiResult<Void>> handleCustomerNotFound(CustomerNotFoundException ex) {
        return fail(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResult<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        String msg = (ex.getMessage() != null && !ex.getMessage().isBlank())
                ? ex.getMessage() : "请求参数或业务逻辑错误";
        return fail(HttpStatus.BAD_REQUEST, "BAD_REQUEST", msg);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResult<Void>> handleAuth(AuthenticationException ex) {
        return fail(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "认证失败");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResult<Void>> handleAccessDenied(AccessDeniedException ex) {
        return fail(HttpStatus.FORBIDDEN, "FORBIDDEN", "无权限访问");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResult<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String msg = ex.getMessage() != null && ex.getMessage().contains("customers_email_key")
                ? "该邮箱已被注册" : "数据操作失败";
        HttpStatus status = msg.contains("邮箱") ? HttpStatus.CONFLICT : HttpStatus.INTERNAL_SERVER_ERROR;
        if (!msg.contains("邮箱")) log.error("Data integrity violation: {}", ex.getMessage(), ex);
        return fail(status, status.getReasonPhrase(), msg);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Void>> handleValidation(MethodArgumentNotValidException ex) {
        var field = ex.getBindingResult().getFieldError();
        String message = field != null ? field.getField() + ": " + field.getDefaultMessage() : "参数验证失败";
        return fail(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleGeneral(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return fail(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "服务器内部错误");
    }
}
