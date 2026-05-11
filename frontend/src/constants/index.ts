/**
 * 应用级常量配置。
 *
 * 常量分类：
 * - API 配置：请求超时、缓存时间
 * - 展示资源：占位图 URL
 * - 状态映射：订单状态到 UI 颜色的对应关系
 *
 * 设计原则：
 * - 所有业务相关的魔数都应集中在此文件
 * - 方便统一调整（如货币符号、缓存策略）
 * - 类型安全的常量导出
 */

/** HTTP 请求超时时间（毫秒）。 */
export const API_TIMEOUT = 10000;

/** 购物车数据缓存时间（毫秒）。30 秒内不自动重新请求。 */
export const CART_STALE_TIME = 30 * 1000;

/** 订单数据缓存时间（毫秒）。60 秒内不自动重新请求。 */
export const ORDERS_STALE_TIME = 60 * 1000;

/** 餐厅数据缓存时间（毫秒）。10 分钟缓存，减少重复请求。 */
export const RESTAURANTS_STALE_TIME = 10 * 60 * 1000;

/** 货币符号。 */
export const CURRENCY_SYMBOL = '$';

/**
 * 图片加载失败时的占位图（SVG 内联 Data URL）。
 * 使用 SVG 而非纯色背景，保留"No Image"文本提示。
 *
 * 注意：两个变体尺寸不同（RestaurantPage 200x150 vs CartPage 60x60），
 * 未来可考虑统一为单一常量并通过 CSS 控制尺寸。
 */
export const PLACEHOLDER_IMAGE = {
  /** 餐厅/菜品卡片占位图（200x150）。 */
  restaurant: 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="200" height="150"%3E%3Crect fill="%23f0f0f0" width="200" height="150"/%3E%3Ctext x="50%25" y="50%25" font-family="sans-serif" font-size="14" fill="%23999" text-anchor="middle" dy=".3em"%3ENo Image%3C/text%3E%3C/svg%3E',
  /** 购物车商品行占位图（60x60）。 */
  cart: 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="60" height="60"%3E%3Crect fill="%23f0f0f0" width="60" height="60"/%3E%3Ctext x="50%25" y="50%25" font-family="sans-serif" font-size="10" fill="%23999" text-anchor="middle" dy=".3em"%3ENo%3C/text%3E%3C/svg%3E',
};
