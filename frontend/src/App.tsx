/**
 * 应用入口组件。
 *
 * 职责：
 * 1. 初始化认证状态验证（启动时检查 localStorage Token 是否有效）
 * 2. 配置全局路由（懒加载 + Suspense）
 * 3. 配置全局错误边界
 * 4. 注册 401 响应拦截回调（Token 过期时软跳转）
 *
 * 认证流程：
 * 1. App 挂载 -> 读取 localStorage 中的 token
 * 2. 若有 token，调用 /auth/me 验证有效性
 * 3. 验证完成前显示加载状态（避免闪现受保护页）
 * 4. 验证完成后正常渲染路由（ProtectedRoute 处理未登录重定向）
 *
 * 懒加载策略：
 * - 所有页面组件使用 React.lazy() + Suspense 实现按需加载
 * - 首屏只需加载 Layout + LoginPage，减少初始 bundle 大小
 */
import { lazy, Suspense, useEffect, useState } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import { Spin } from 'antd';
import Layout from './components/Layout';
import ProtectedRoute from './components/ProtectedRoute';
import ErrorBoundary from './components/ErrorBoundary';
import { useAuthStore } from './stores/authStore';
import { setUnauthorizedHandler } from './api/apiClient';

// 懒加载页面组件
const LoginPage = lazy(() => import('./pages/LoginPage'));
const RegisterPage = lazy(() => import('./pages/RegisterPage'));
const RestaurantPage = lazy(() => import('./pages/RestaurantPage'));
const CartPage = lazy(() => import('./pages/CartPage'));
const OrdersPage = lazy(() => import('./pages/OrdersPage'));

// 认证检查等待页
const AuthCheckFallback = () => (
  <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
    <Spin size="large" />
  </div>
);

const App = () => {
  const [checked, setChecked] = useState(false);
  const token = useAuthStore((s) => s.token);
  const validateToken = useAuthStore((s) => s.validateToken);
  const logout = useAuthStore((s) => s.logout);

  // 注册 401 软跳转处理器
  useEffect(() => {
    setUnauthorizedHandler(() => {
      logout();
      // 使用 Navigate 组件实现软跳转，保持 SPA 状态
      window.history.pushState(null, '', '/login');
      window.dispatchEvent(new PopStateEvent('popstate'));
    });
  }, [logout]);

  useEffect(() => {
    const checkAuth = async () => {
      if (token) {
        await validateToken();
      }
      setChecked(true);
    };
    checkAuth();
  }, [token, validateToken]);

  if (!checked) {
    return <AuthCheckFallback />;
  }

  return (
    <ErrorBoundary>
      <Layout>
        <Suspense fallback={<div style={{ textAlign: 'center', marginTop: 100 }}><Spin size="large" /></div>}>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route
              path="/restaurants"
              element={
                <ProtectedRoute>
                  <RestaurantPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/cart"
              element={
                <ProtectedRoute>
                  <CartPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/orders"
              element={
                <ProtectedRoute>
                  <OrdersPage />
                </ProtectedRoute>
              }
            />
            <Route path="/" element={<Navigate to="/restaurants" replace />} />
            <Route path="*" element={<Navigate to="/restaurants" replace />} />
          </Routes>
        </Suspense>
      </Layout>
    </ErrorBoundary>
  );
};

export default App;
