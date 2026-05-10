/**
 * 应用入口。
 * 包含启动时 Token 验证逻辑，确保 localStorage 中的 Token 仍然有效。
 */
import { useEffect, useState } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import { Spin } from 'antd';
import Layout from './components/Layout';
import ProtectedRoute from './components/ProtectedRoute';
import ErrorBoundary from './components/ErrorBoundary';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import RestaurantPage from './pages/RestaurantPage';
import CartPage from './pages/CartPage';
import OrdersPage from './pages/OrdersPage';
import { useAuthStore } from './stores/authStore';

const App = () => {
  const [checked, setChecked] = useState(false);
  const { token, validateToken } = useAuthStore();

  useEffect(() => {
    const checkAuth = async () => {
      if (token) {
        await validateToken();
      }
      setChecked(true);
    };
    checkAuth();
    // Intent: run once on mount to validate stored token
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (!checked) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <Spin size="large" />
      </div>
    );
  }

  return (
    <ErrorBoundary>
      <Layout>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/restaurants" element={<ProtectedRoute><RestaurantPage /></ProtectedRoute>} />
          <Route path="/cart" element={<ProtectedRoute><CartPage /></ProtectedRoute>} />
          <Route path="/orders" element={<ProtectedRoute><OrdersPage /></ProtectedRoute>} />
          <Route path="/" element={<Navigate to="/restaurants" replace />} />
          <Route path="*" element={<Navigate to="/restaurants" replace />} />
        </Routes>
      </Layout>
    </ErrorBoundary>
  );
};

export default App;
