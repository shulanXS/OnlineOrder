/**
 * Cart API.
 *
 * 封装购物车相关的 HTTP 请求。
 *
 * 接口约定：
 * - 获取购物车：GET /cart -> ApiResult<Cart>
 * - 添加商品：POST /cart -> ApiResult<Cart>
 * - 更新数量：POST /cart/items/{menuItemId} -> ApiResult<Cart>
 * - 结账：POST /cart/checkout -> ApiResult<Order>
 *
 * 注意：apiClient 拦截器已自动解包 ApiResult，
 * then 回调中 res 已是解包后的数据（不再是 AxiosResponse）。
 */
import apiClient from './apiClient';
import type { Cart, Order } from '../types/api';

/**
 * 获取当前用户的购物车。
 *
 * @returns Promise<Cart> - 购物车完整信息（含商品行）
 */
export const getCart = (): Promise<Cart> => {
  return apiClient.get<Cart>('/cart')
    .then((res) => (res as unknown as Cart));
};

/**
 * 添加菜品到购物车。
 *
 * @param menuId - 菜品 ID
 * @returns Promise<Cart> - 更新后的购物车完整信息
 */
export const addItemToCart = (menuId: number): Promise<Cart> => {
  return apiClient.post<Cart>('/cart', { menuId })
    .then((res) => (res as unknown as Cart));
};

/**
 * 更新购物车中指定商品的数量。
 *
 * @param menuItemId - 菜品 ID
 * @param quantity - 新数量（0 表示移除）
 * @returns Promise<Cart> - 更新后的购物车完整信息
 */
export const updateItemQuantity = (menuItemId: number, quantity: number): Promise<Cart> => {
  return apiClient.post<Cart>(`/cart/items/${menuItemId}`, { quantity })
    .then((res) => (res as unknown as Cart));
};

/**
 * 结账：清空购物车并生成订单。
 *
 * 后端返回 OrderDto（订单信息）而非 CartDto。
 * 成功后应：
 * 1. 将购物车 query data 重置为空购物车状态
 * 2. 刷新订单列表
 *
 * @returns Promise<Order> - 生成的订单信息
 */
export const checkout = (): Promise<Order> => {
  return apiClient.post<Order>('/cart/checkout')
    .then((res) => (res as unknown as Order));
};
