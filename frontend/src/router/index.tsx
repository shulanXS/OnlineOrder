/**
 * 路由配置文件。
 *
 * 集中声明所有路由规则，提供路由元数据供其他模块使用。
 *
 * 设计原则：
 * - 路由按访问权限分为公开和受保护两类
 * - 目前 App.tsx 直接使用路由组件，routes 数组提供元数据支持（如权限判断、面包屑等）
 * - 未来可扩展：异步加载路由配置、支持嵌套路由、支持路由元数据（标题、图标等）
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
 * 供需要路由元数据的模块使用（如权限控制、导航高亮等）。
 */
export const routes: RouteConfig[] = [
  { path: '/login', public: true },
  { path: '/register', public: true },
  { path: '/restaurants' },
  { path: '/cart' },
  { path: '/orders' },
  { path: '/' },
];
