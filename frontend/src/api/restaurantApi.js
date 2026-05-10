/**
 * Restaurant API.
 *
 * 封装餐厅和菜品相关的 HTTP 请求。
 */

import apiClient from './apiClient';

/**
 * 获取所有餐厅及其菜品的完整列表。
 * 后端通过嵌套结构一次性返回所有数据，前端无需多次请求。
 *
 * @returns {Promise<Array<{id: number, name: string, address: string, phone: string,
 *   imageUrl: string, menuItems: Array<{id: number, name: string, description: string,
 *   price: number, imageUrl: string}>}>}
 */
export const getRestaurants = () => {
  return apiClient.get('/restaurants/menu').then((res) => res.data);
};
