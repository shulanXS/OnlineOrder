/**
 * 格式化工具函数。
 *
 * 提供金额、日期等通用格式化逻辑，避免在多个页面中重复定义。
 *
 * 设计原则：
 * - 所有格式化函数均为纯函数，无副作用，便于测试
 * - 使用 JavaScript Intl API 替代 toFixed/toLocaleString，获得更好的国际化支持
 */

/**
 * 格式化金额为本地货币字符串。
 *
 * @param price - 价格（支持 number、string、null、undefined）
 * @returns 格式化后的金额字符串，如 "$10.59"
 *
 * 使用 Intl.NumberFormat 确保：
 * - 最多 2 位小数（toFixed(2)）
 * - 自动添加货币符号前缀
 * - 负数也能正确处理
 */
export const formatPrice = (price: number | string | null | undefined): string => {
  const n = Number(price) || 0;
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(n);
};

/**
 * 格式化日期为本地可读字符串。
 *
 * @param dateStr - ISO 8601 格式的日期字符串
 * @returns 格式化的日期时间字符串，如 "2024/01/15 14:30"
 *
 * 使用 Date 对象构造，支持大多数标准格式。
 * 注意：如果传入非标准格式可能导致 Invalid Date。
 */
export const formatDate = (dateStr: string): string => {
  const d = new Date(dateStr);
  if (isNaN(d.getTime())) return dateStr;
  return d.toLocaleString('en-US', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  });
};
