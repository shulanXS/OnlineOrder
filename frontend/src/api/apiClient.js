/**
 * Axios API Client.
 *
 * 基于 axios 的 HTTP 客户端封装，提供统一的请求/响应拦截处理。
 *
 * 设计要点：
 * - 请求拦截器：自动附加 Authorization Header（从 Zustand store 读取 Token）
 * - 响应拦截器：统一处理 HTTP 错误状态码，返回结构化错误信息
 * - 401 响应：Token 过期时清除本地认证状态并显示中文提示
 */

import axios from 'axios';
import { message } from 'antd';
import { useAuthStore } from '../stores/authStore';

const BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

const apiClient = axios.create({
  baseURL: BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器：附加认证 Token
apiClient.interceptors.request.use(
  (config) => {
    const token = useAuthStore.getState().token;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 响应拦截器：统一错误处理
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (!error.response) {
      // 网络错误（如断网、超时）
      return Promise.reject(
        new Error('网络连接失败，请检查网络是否正常')
      );
    }

    const { status, data } = error.response;

    switch (status) {
      case 401:
        useAuthStore.getState().logout();
        message.error('登录已过期，请重新登录');
        window.location.href = '/login';
        return Promise.reject(new Error('登录已过期，请重新登录'));
      case 403:
        return Promise.reject(new Error(data?.message || '您没有权限访问此资源'));
      case 404:
        return Promise.reject(new Error(data?.message || '请求的资源不存在'));
      case 400:
        return Promise.reject(new Error(data?.message || '请求参数错误'));
      case 409:
        return Promise.reject(new Error(data?.message || '数据冲突'));
      case 500:
      default:
        return Promise.reject(
          new Error(data?.message || '服务器内部错误，请稍后重试')
        );
    }
  }
);

export default apiClient;
