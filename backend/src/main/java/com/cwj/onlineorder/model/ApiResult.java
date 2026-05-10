package com.cwj.onlineorder.model;

/**
 * 统一 API 响应包装器。
 *
 * 所有 Controller 接口统一返回此结构：
 * - 成功：{ "success": true, "data": {...} }
 * - 失败：{ "success": false, "error": {...} }
 *
 * 这使前端可以基于统一契约解析所有响应，无需针对每个接口做不同处理。
 */
public record ApiResult<T>(
        boolean success,
        T data,
        ErrorInfo error
) {
    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(true, data, null);
    }

    public static <T> ApiResult<T> fail(int status, String code, String message) {
        return new ApiResult<>(false, null, new ErrorInfo(status, code, message));
    }

    public record ErrorInfo(int status, String code, String message) {}
}
