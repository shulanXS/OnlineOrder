/**
 * Restaurant API.
 *
 * 封装餐厅和菜品相关的 HTTP 请求。
 *
 * 接口约定：
 * - 获取所有餐厅及菜品：GET /restaurants/menu -> ApiResult<Restaurant[]>
 *
 * 后端实现：一次性返回所有餐厅及其菜品（嵌套结构），
 * 前端无需多次请求，通过 staleTime 控制缓存。
 *
 * 注意：apiClient 拦截器已自动解包 ApiResult，
 * then 回调中 res 已是解包后的数据（不再是 AxiosResponse）。
 */
import apiClient from './apiClient';
import type { Restaurant } from '../types/api';

/**
 * 获取所有餐厅及其菜品的完整列表。
 *
 * @returns Promise<Restaurant[]> - 餐厅列表，每项包含嵌套的 menuItems
 * @throws 后端返回 401 时由 apiClient 拦截器统一处理
 */
export const getRestaurants = (): Promise<Restaurant[]> => {
  return apiClient.get<Restaurant[]>('/restaurants/menu')
    .then((res) => (res as unknown as Restaurant[]));
};
