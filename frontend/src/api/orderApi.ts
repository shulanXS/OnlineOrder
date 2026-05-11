/**
 * Order API.
 *
 * 封装订单相关的 HTTP 请求。
 *
 * 接口约定：
 * - 获取订单列表：GET /orders -> ApiResult<Order[]>
 *
 * 注意：apiClient 拦截器已自动解包 ApiResult，
 * then 回调中 res 已是解包后的数据（不再是 AxiosResponse）。
 */
import apiClient from './apiClient';
import type { Order } from '../types/api';

/**
 * 获取当前用户的订单历史。
 *
 * @returns Promise<Order[]> - 订单列表（含明细），按创建时间倒序排列
 * @throws 后端返回 401 时由 apiClient 拦截器统一处理
 */
export const getOrders = (): Promise<Order[]> => {
  return apiClient.get<Order[]>('/orders')
    .then((res) => (res as unknown as Order[]));
};
