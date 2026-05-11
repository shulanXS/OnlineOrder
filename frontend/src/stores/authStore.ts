/**
 * Auth Store.
 *
 * 基于 Zustand 的全局认证状态管理。
 *
 * 管理的数据：
 * - token：JWT 访问令牌（持久化到 localStorage）
 * - user：当前登录用户的详细信息（内存中，不持久化）
 * - isAuthenticated：派生状态，始终等于 !!token
 *
 * 设计原则：
 * - token 持久化：页面刷新后仍能保持登录状态
 * - user 不持久化：仅在登录时加载，页面刷新时重新获取
 * - isAuthenticated 是派生状态，无需单独存储
 *
 * 状态流转：
 * 1. 初始状态：token=null, user=null, isAuthenticated=false
 * 2. 登录成功后：token=有效JWT, user=用户信息, isAuthenticated=true
 * 3. Token 过期或主动登出：token=null, user=null, isAuthenticated=false
 *
 * 与 apiClient 的协作：
 * - 请求拦截器从 token 读取 Bearer Token
 * - 401 响应由 apiClient 拦截器通过 unauthorizedCallback 通知
 * - unauthorizedCallback 回调会调用 logout() 并软跳转登录页
 */
import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { getCurrentUser, validateToken as checkToken } from '../api/authApi';
import type { User } from '../types/api';

interface AuthState {
  token: string | null;
  user: User | null;
  isAuthenticated: boolean;
  setAuth: (token: string) => Promise<void>;
  logout: () => void;
  validateToken: () => Promise<boolean>;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      user: null,
      isAuthenticated: false,

      setAuth: async (token: string) => {
        set({ token, user: null, isAuthenticated: true });
        try {
          const user = await getCurrentUser();
          set({ user });
        } catch {
          set({ user: null });
        }
      },

      logout: () => {
        set({ token: null, user: null, isAuthenticated: false });
      },

      validateToken: async () => {
        const { token } = get();
        if (!token) return false;
        const valid = await checkToken();
        if (!valid) {
          get().logout();
        }
        return valid;
      },
    }),
    { name: 'auth-storage' }
  )
);
