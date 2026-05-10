/**
 * Order API.
 *
 * 封装订单相关的 HTTP 请求。
 */

import apiClient from './apiClient';

/**
 * @typedef {Object} OrderDetailDto
 * @property {number} id - 订单明细 ID
 * @property {number} menuItemId - 菜品 ID（快照）
 * @property {string} menuItemName - 菜品名称（快照）
 * @property {string} menuItemDescription - 菜品描述（快照）
 * @property {string} menuItemImageUrl - 菜品图片 URL（快照）
 * @property {number} price - 快照单价
 * @property {number} quantity - 购买数量
 */

/**
 * @typedef {Object} OrderDto
 * @property {number} id - 订单 ID
 * @property {number} customerId - 顾客 ID
 * @property {string} status - 订单状态（PENDING/CONFIRMED/PREPARING/SHIPPING/COMPLETED/CANCELLED）
 * @property {number} totalPrice - 订单总价
 * @property {string} createdAt - 下单时间（ISO 8601）
 * @property {string} updatedAt - 更新时间（ISO 8601）
 * @property {OrderDetailDto[]} details - 订单明细列表
 */

/**
 * 获取当前用户的订单历史。
 *
 * @returns {Promise<OrderDto[]>} 订单列表
 */
export const getOrders = () => {
  return apiClient.get('/orders').then((res) => res.data);
};
