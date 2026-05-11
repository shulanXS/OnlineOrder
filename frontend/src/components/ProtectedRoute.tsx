/**
 * ProtectedRoute 组件。
 *
 * 路由保护高阶组件：
 * - 已登录：正常渲染子路由（children）
 * - 未登录：重定向到 /login，登录后返回原页面（通过 location state 实现）
 *
 * 设计要点：
 * - 使用 Navigate 组件的 state={{ from: location }} 传递原页面路径
 * - replace 模式替换历史记录，避免登录页回退到受保护页
 * - 纯 UI 组件，不持有业务状态，所有状态从 Zustand authStore 读取
 */
import { Navigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../stores/authStore';

const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  // 从 Zustand store 读取认证状态（isAuthenticated 是派生状态）
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  const location = useLocation();

  if (!isAuthenticated) {
    // 将用户重定向到登录页，并记录当前路径
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return <>{children}</>;
};

export default ProtectedRoute;
