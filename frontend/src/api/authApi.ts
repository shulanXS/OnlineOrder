/**
 * Auth API.
 *
 * 封装认证相关的 HTTP 请求，包括登录、注册、获取用户信息、Token 验证。
 *
 * 接口约定：
 * - 登录：POST /auth/login  -> ApiResult<AuthResponse>
 * - 注册：POST /auth/register -> ApiResult<AuthResponse>
 * - 用户信息：GET /auth/me -> ApiResult<User>
 *
 * 设计要点：
 * - 登录/注册成功后返回 accessToken，前端保存到 Zustand store
 * - validateToken 通过调用 /auth/me 判断 Token 是否有效
 * - apiClient 拦截器已自动解包 ApiResult，then 回调中 res 已是解包后的数据
 */
import apiClient from './apiClient';
import type { AuthResponse, LoginCredentials, RegisterData, User } from '../types/api';

/**
 * 用户登录。
 *
 * @param credentials - 登录凭据，包含用户名（邮箱）和密码
 * @returns Promise<AuthResponse> - 包含 accessToken 等认证信息
 */
export const login = (credentials: LoginCredentials): Promise<AuthResponse> => {
  return apiClient.post<AuthResponse>('/auth/login', credentials)
    .then((res) => (res as unknown as AuthResponse));
};

/**
 * 用户注册。
 *
 * @param data - 注册信息，包含邮箱、密码、姓名
 * @returns Promise<AuthResponse> - 注册成功后直接返回 Token，无需二次登录
 */
export const register = (data: RegisterData): Promise<AuthResponse> => {
  return apiClient.post<AuthResponse>('/auth/register', data)
    .then((res) => (res as unknown as AuthResponse));
};

/**
 * 获取当前登录用户信息。
 *
 * @returns Promise<User> - 当前登录用户的详细信息
 * @throws 后端返回 401 时由 apiClient 拦截器统一处理
 */
export const getCurrentUser = (): Promise<User> => {
  return apiClient.get<User>('/auth/me')
    .then((res) => (res as unknown as User));
};

/**
 * 验证当前 Token 是否有效。
 *
 * 通过调用 /auth/me 接口来判断 Token 状态：
 * - 2xx 响应：Token 有效
 * - 其他状态：Token 无效或已过期
 *
 * 注意：logout 逻辑已由 apiClient 拦截器统一处理（unauthorizedCallback），
 * 此处只需返回布尔值即可。
 *
 * @returns Promise<boolean> - true 表示已认证，false 表示未认证
 */
export const validateToken = (): Promise<boolean> => {
  return apiClient
    .get('/auth/me')
    .then(() => true)
    .catch(() => false);
};
