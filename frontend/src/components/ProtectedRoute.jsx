/**
 * ProtectedRoute 组件。
 *
 * 路由保护高阶组件：
 * - 已登录：正常渲染子路由
 * - 未登录：重定向到登录页，登录后返回原页面
 */

import { Navigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../stores/authStore';

const ProtectedRoute = ({ children }) => {
  const { isAuthenticated } = useAuthStore();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return children;
};

export default ProtectedRoute;
