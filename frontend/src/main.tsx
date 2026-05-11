/**
 * 应用入口文件（main.tsx）。
 *
 * 职责：
 * 1. 创建 React 根节点
 * 2. 配置 React Query（全局数据获取和缓存管理）
 * 3. 配置路由（BrowserRouter）
 * 4. 渲染根组件
 *
 * QueryClient 配置说明：
 * - retry: 1  -> 请求失败时自动重试 1 次（避免临时网络抖动）
 * - staleTime: 5分钟  -> 数据在 5 分钟内不会自动重新请求
 * - refetchOnWindowFocus: false  -> 切换标签页回来不触发重新请求
 */
import React from 'react';
import ReactDOM from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter } from 'react-router-dom';
import App from './App';
import './index.css';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 5 * 60 * 1000,
      refetchOnWindowFocus: false,
    },
  },
});

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </QueryClientProvider>
  </React.StrictMode>
);
