/**
 * useOrders Hook.
 * 封装订单历史数据的获取。
 */
import { useQuery } from '@tanstack/react-query';
import { getOrders } from '../api/orderApi';

export const useOrders = () => {
  return useQuery({
    queryKey: ['orders'],
    queryFn: getOrders,
    staleTime: 60 * 1000,
  });
};
