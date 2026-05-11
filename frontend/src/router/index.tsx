/**
 * 集中路由配置。
 *
 * 此模块集中声明所有路由规则，App.tsx 统一消费。
 * 提供路由元数据（路径、是否公开）供其他模块使用。
 *
 * 路由分层：
 * - 公开路由：/login、/register（未登录可访问）
 * - 受保护路由：/restaurants、/cart、/orders（需登录）
 * - 根路由：/ 跳转至 /restaurants
 * - 通配路由：* 兜底跳转到 /restaurants
 */
import type { ReactNode } from 'react';

/**
 * 路由配置项。
 * @property path - 路由路径
 * @property public - 是否为公开路由（无需登录）
 * @property children - 子路由（当前为空，预留扩展）
 */
export interface RouteConfig {
  path: string;
  public?: boolean;
  children?: ReactNode;
}

/**
 * 所有路由声明。
 *
 * 设计原则：
 * - 路由按访问权限分为公开和受保护两类
 * - 未来可扩展：异步加载路由配置、支持嵌套路由、支持路由元数据（标题、图标等）
 */
export const routes: RouteConfig[] = [
  { path: '/login', public: true },
  { path: '/register', public: true },
  { path: '/restaurants' },
  { path: '/cart' },
  { path: '/orders' },
  { path: '/' },
];
