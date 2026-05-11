package com.cwj.onlineorder.model;

/**
 * 统一 API 响应包装器。
 *
 * 所有 Controller 接口统一返回此结构：
 * - 成功：{ "success": true, "data": {...} }
 * - 失败：{ "success": false, "data": null, "error": {...} }
 *
 * 设计要点：
 * - 前端可以基于 success 字段判断请求是否成功
 * - 错误信息包含 HTTP 状态码、业务错误码和人类可读的消息
 * - error 字段为 ErrorInfo 内部 record，仅在失败时填充
 *
 * @param <T>    成功时 data 字段的类型
 * @param success 请求是否成功
 * @param data   成功时的响应数据
 * @param error  失败时的错误信息
 */
public record ApiResult<T>(
        boolean success,
        T data,
        ErrorInfo error
) {
    /**
     * 构建成功响应。
     */
    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(true, data, null);
    }

    /**
     * 构建失败响应。
     *
     * @param status  HTTP 状态码
     * @param code   业务错误码（如 VALIDATION_ERROR、NOT_FOUND）
     * @param message 人类可读的错误描述
     */
    public static <T> ApiResult<T> fail(int status, String code, String message) {
        return new ApiResult<>(false, null, new ErrorInfo(status, code, message));
    }

    /**
     * 错误信息结构。
     *
     * @param status  HTTP 状态码
     * @param code   业务错误码
     * @param message 错误描述
     */
    public record ErrorInfo(int status, String code, String message) {}
}
