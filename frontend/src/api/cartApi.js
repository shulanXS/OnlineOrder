/**
 * Cart API.
 *
 * 封装购物车相关的 HTTP 请求。
 *
 * 响应数据格式（CartDto）：
 * @typedef {Object} CartDto
 * @property {number|null} id - 购物车 ID
 * @property {number} totalPrice - 购物车总价
 * @property {CartItemDto[]} cartItems - 购物车商品行列表
 *
 * @typedef {Object} CartItemDto
 * @property {number} cartItemId - 购物车商品行 ID
 * @property {number} menuItemId - 菜品 ID
 * @property {number} restaurantId - 餐厅 ID
 * @property {number} price - 快照单价
 * @property {number} quantity - 数量
 * @property {string} menuItemName - 菜品名称
 * @property {string} menuItemDescription - 菜品描述
 * @property {string} menuItemImageUrl - 菜品图片 URL
 */

import apiClient from './apiClient';

/**
 * 获取当前用户的购物车。
 *
 * @returns {Promise<CartDto>} 购物车数据
 */
export const getCart = () => {
  return apiClient.get('/cart').then((res) => res.data);
};

/**
 * 添加商品到购物车。
 *
 * @param {number} menuId - 菜品 ID
 * @returns {Promise<CartDto>} 添加后的完整购物车数据
 */
export const addItemToCart = (menuId) => {
  return apiClient.post('/cart', { menuId }).then((res) => res.data);
};

/**
 * 更新购物车中指定商品的数量。
 *
 * @param {number} menuItemId - 菜品 ID
 * @param {number} quantity - 新数量（设为 0 时删除该商品）
 * @returns {Promise<CartDto>} 更新后的完整购物车数据
 */
export const updateItemQuantity = (menuItemId, quantity) => {
  return apiClient
    .post(`/cart/items/${menuItemId}`, { quantity })
    .then((res) => res.data);
};

/**
 * 结账：清空购物车并生成订单。
 *
 * @returns {Promise<void>} 成功后跳转到订单页
 */
export const checkout = () => {
  return apiClient.post('/cart/checkout').then((res) => res.data);
};
