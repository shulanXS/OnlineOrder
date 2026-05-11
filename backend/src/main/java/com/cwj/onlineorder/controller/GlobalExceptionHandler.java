package com.cwj.onlineorder.controller;

import com.cwj.onlineorder.exception.CustomerNotFoundException;
import com.cwj.onlineorder.exception.ForbiddenException;
import com.cwj.onlineorder.exception.MenuItemNotFoundException;
import com.cwj.onlineorder.model.ApiResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.validation.ConstraintViolationException;

/**
 * 全局异常处理器。
 * 统一返回 ApiResult 结构的错误响应。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final int PG_UNIQUE_VIOLATION = 23505;

    private ResponseEntity<ApiResult<Void>> fail(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(ApiResult.fail(status.value(), code, message));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResult<Void>> handleForbidden(ForbiddenException ex) {
        return fail(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage());
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
        Throwable cause = ex.getCause();
        if (cause instanceof org.postgresql.util.PSQLException pgEx
                && pgEx.getSQLState() != null
                && pgEx.getSQLState().equals(String.valueOf(PG_UNIQUE_VIOLATION))) {
            return fail(HttpStatus.CONFLICT, "CONFLICT", "该邮箱已被注册");
        }
        log.error("Data integrity violation: {}", ex.getMessage(), ex);
        return fail(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "数据操作失败");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResult<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        String msg = ex.getConstraintViolations().isEmpty()
                ? "数据约束违反"
                : ex.getConstraintViolations().iterator().next().getMessage();
        return fail(HttpStatus.BAD_REQUEST, "CONSTRAINT_VIOLATION", msg);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiResult<Void>> handleOptimisticLock(OptimisticLockingFailureException ex) {
        return fail(HttpStatus.CONFLICT, "CONFLICT", "数据已被其他操作修改，请刷新后重试");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResult<Void>> handleMissingParam(MissingServletRequestParameterException ex) {
        return fail(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER",
                "缺少必需参数: " + ex.getParameterName());
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
