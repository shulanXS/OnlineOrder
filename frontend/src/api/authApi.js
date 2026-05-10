/**
 * Auth API.
 *
 * 封装认证相关的 HTTP 请求：
 * - 登录
 * - 注册
 * - 获取当前用户信息
 * - Token 验证
 */

import apiClient from './apiClient';
import { useAuthStore } from '../stores/authStore';

/**
 * 用户登录。
 *
 * @param {Object} credentials - 登录凭据
 * @param {string} credentials.username - 用户名/邮箱
 * @param {string} credentials.password - 密码
 * @returns {Promise<{accessToken: string, tokenType: string, expiresIn: number}>}
 */
export const login = (credentials) => {
  return apiClient
    .post('/auth/login', {
      username: credentials.username,
      password: credentials.password,
    })
    .then((res) => res.data);
};

/**
 * 用户注册。
 *
 * @param {Object} data - 注册信息
 * @param {string} data.email - 邮箱
 * @param {string} data.password - 密码
 * @param {string} data.firstName - 名
 * @param {string} data.lastName - 姓
 * @returns {Promise<{accessToken: string, tokenType: string, expiresIn: number}>}
 */
export const register = (data) => {
  return apiClient
    .post('/auth/register', {
      email: data.email,
      password: data.password,
      firstName: data.firstName,
      lastName: data.lastName,
    })
    .then((res) => res.data);
};

/**
 * 获取当前登录用户信息。
 *
 * @returns {Promise<{id: number, email: string, firstName: string, lastName: string}>}
 * @throws 如果 Token 无效或请求失败则抛出异常
 */
export const getCurrentUser = () => {
  return apiClient
    .get('/auth/me')
    .then((res) => res.data);
};

/**
 * 验证当前 Token 是否有效。
 * 若后端返回 401，清除本地认证状态。
 *
 * @returns {Promise<boolean>} true 表示已认证，false 表示未认证
 */
export const validateToken = () => {
  return apiClient
    .get('/auth/me')
    .then((res) => res.status >= 200 && res.status < 300)
    .catch((err) => {
      if (err.response?.status === 401) {
        useAuthStore.getState().logout();
      }
      return false;
    });
};
