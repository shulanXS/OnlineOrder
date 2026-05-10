/**
 * Auth Store.
 *
 * 管理认证状态：Token、用户信息、登录状态。
 *
 * 设计原则：
 * - setAuth：异步设置认证状态，同时获取并缓存用户信息
 * - validateToken：通过 /auth/me 验证 Token 有效性，无效则清除状态
 * - logout：清除所有认证状态
 * - 401 错误由 apiClient 拦截器处理（自动调用 logout）
 */
import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { getCurrentUser, validateToken as checkToken } from '../api/authApi';

export const useAuthStore = create(
  persist(
    (set, get) => ({
      token: null,
      user: null,
      isAuthenticated: false,

      /**
       * 设置认证状态并获取用户信息。
       * 登录/注册成功后调用。
       */
      setAuth: async (token) => {
        set({ token, isAuthenticated: true });
        try {
          const user = await getCurrentUser();
          set({ user });
        } catch {
          set({ user: null });
        }
      },

      /**
       * 清除认证状态。
       * 登出或 401 时调用。
       */
      logout: () => {
        set({ token: null, user: null, isAuthenticated: false });
      },

      /**
       * 验证当前 Token 是否有效。
       * 无效时自动清除认证状态。
       * @returns {Promise<boolean>} true 表示有效，false 表示无效
       */
      validateToken: async () => {
        if (!get().token) return false;
        const valid = await checkToken();
        if (!valid) get().logout();
        return valid;
      },
    }),
    { name: 'auth-storage' }
  )
);
