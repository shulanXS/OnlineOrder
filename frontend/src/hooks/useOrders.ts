/**
 * useOrders Hook.
 * 封装订单历史数据的获取逻辑。
 *
 * 使用 React Query 进行数据获取和缓存管理：
 * - queryKey: 唯一标识此查询，用于缓存失效和手动刷新
 * - queryFn: 实际发起 HTTP 请求的异步函数
 * - staleTime: 数据"过期"时间，在此时间内不会自动重新请求
 *
 * 缓存策略：
 * - 订单数据变化不频繁，staleTime 设置为 60 秒
 * - 订单页面重新挂载（navigate 回来）不会强制刷新，除非数据已过期
 */
import { useQuery } from '@tanstack/react-query';
import { getOrders } from '../api/orderApi';
import { ORDERS_STALE_TIME } from '../constants';

export const useOrders = () => {
  return useQuery({
    queryKey: ['orders'],
    queryFn: getOrders,
    staleTime: ORDERS_STALE_TIME,
  });
};
