/**
 * Axios API Client.
 *
 * 基于 axios 的 HTTP 客户端封装，提供统一的请求/响应拦截处理。
 *
 * 设计要点：
 * - 请求拦截器：自动附加 Authorization Header（从 Zustand store 读取 Token）
 * - 响应拦截器：统一处理 HTTP 错误状态码，返回结构化错误信息
 * - 401 响应：通过回调机制通知外部进行软跳转，避免 window.location 破坏 SPA 状态
 * - ApiResult 兼容：自动解包后端 ApiResult 包装层（success=true -> data 部分）
 *
 * ApiResult 兼容性：
 * - 后端所有成功响应统一包装在 ApiResult 中：{ success: true, data: {...}, error: null }
 * - 此拦截器自动解包，调用方直接拿到 data 部分，无需手动 .then(res => res.data)
 */
import axios, { AxiosError, AxiosResponse } from 'axios';
import { API_TIMEOUT } from '../constants';
import { useAuthStore } from '../stores/authStore';

const BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

let unauthorizedCallback: (() => void) | null = null;

export const setUnauthorizedHandler = (cb: () => void) => {
  unauthorizedCallback = cb;
};

const apiClient = axios.create({
  baseURL: BASE_URL,
  timeout: API_TIMEOUT,
  headers: {
    'Content-Type': 'application/json',
  },
});

const HTTP = {
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  BAD_REQUEST: 400,
  CONFLICT: 409,
  SERVER_ERROR: 500,
} as const;

const DEFAULT_ERROR: Record<number, string> = {
  [HTTP.UNAUTHORIZED]: '登录已过期，请重新登录',
  [HTTP.FORBIDDEN]: '您没有权限访问此资源',
  [HTTP.NOT_FOUND]: '请求的资源不存在',
  [HTTP.BAD_REQUEST]: '请求参数错误',
  [HTTP.CONFLICT]: '数据冲突',
  [HTTP.SERVER_ERROR]: '服务器内部错误，请稍后重试',
};

interface ApiResultPayload {
  success: boolean;
  data: unknown;
  error: { status: number; code: string; message: string } | null;
}

interface ErrorPayload {
  message?: string;
  error?: { message?: string; code?: string };
  success?: boolean;
}

// 请求拦截器：附加认证 Token
apiClient.interceptors.request.use(
  (config) => {
    const { token } = useAuthStore.getState();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 响应拦截器：自动解包 ApiResult，返回 response.data 给调用方
apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    const payload = response.data;

    // 检测并解包 ApiResult 包装层
    if (
      payload !== null &&
      typeof payload === 'object' &&
      !Array.isArray(payload) &&
      'success' in payload
    ) {
      const result = payload as ApiResultPayload;
      if (result.success) {
        // 解包成功：直接返回 data 部分
        return result.data as unknown as AxiosResponse;
      }
    }

    // 非 ApiResult 格式（如原始数据），透传 data
    return response;
  },
  (error: AxiosError<ErrorPayload>) => {
    if (!error.response) {
      return Promise.reject(new Error('网络连接失败，请检查网络是否正常'));
    }

    const { status, data } = error.response;
    const errorMessage =
      (data as ErrorPayload)?.error?.message ??
      (data as ErrorPayload)?.message ??
      DEFAULT_ERROR[status] ??
      '请求失败，请稍后重试';

    switch (status) {
      case HTTP.UNAUTHORIZED:
        unauthorizedCallback?.();
        return Promise.reject(new Error(errorMessage));
      default:
        return Promise.reject(new Error(errorMessage));
    }
  }
);

export default apiClient;
