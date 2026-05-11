/**
 * useCart Hook.
 *
 * 封装购物车数据的获取和操作。
 *
 * React Query 配置说明：
 * - queryKey: 唯一标识，用于缓存失效和手动刷新
 * - staleTime: 30 秒内不自动重新请求
 * - refetchOnMount: 每次挂载时重新请求，确保数据最新
 * - enabled: 是否启用查询（未登录时不发请求，避免 401）
 *
 * 乐观更新策略（useUpdateQuantity）：
 * - onMutate：在请求发出前直接更新 UI，提升响应速度
 * - onError：请求失败时回滚到之前的状态
 * - onSettled：无论成功失败都刷新服务器数据，确保一致性
 */
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getCart, addItemToCart, updateItemQuantity, checkout } from '../api/cartApi';
import { CART_STALE_TIME } from '../constants';
import type { Cart } from '../types/api';

interface UseCartOptions {
  /** 是否启用查询。未登录时应传 false，避免产生 401 请求。 */
  enabled?: boolean;
}

export const useCart = (options: UseCartOptions = {}) => {
  const { enabled = true } = options;
  return useQuery({
    queryKey: ['cart'],
    queryFn: getCart,
    staleTime: CART_STALE_TIME,
    refetchOnMount: true,
    // 未登录时不发请求，直接返回缓存或 undefined
    enabled,
    // 请求失败时不要自动重试，避免在 401 后反复请求
    retry: false,
  });
};

export const useAddToCart = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: addItemToCart,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['cart'] }),
  });
};

export const useUpdateQuantity = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ menuItemId, quantity }: { menuItemId: number; quantity: number }) =>
      updateItemQuantity(menuItemId, quantity),
    onMutate: async ({ menuItemId, quantity }) => {
      await queryClient.cancelQueries({ queryKey: ['cart'] });
      const prev = queryClient.getQueryData<Cart>(['cart']);
      queryClient.setQueryData<Cart>(['cart'], (old) => {
        if (!old) return old;
        const items = (old.cartItems || []).map((item) =>
          item.menuItemId === menuItemId ? { ...item, quantity } : item
        );
        const totalPrice = items.reduce((s, i) => s + Number(i.price) * i.quantity, 0);
        return { ...old, cartItems: items, totalPrice };
      });
      return { prev };
    },
    onError: (_e, _v, ctx) => {
      // 回滚到乐观更新前的状态
      if (ctx?.prev !== undefined) {
        queryClient.setQueryData<Cart>(['cart'], ctx.prev);
      }
    },
    onSettled: () => queryClient.invalidateQueries({ queryKey: ['cart'] }),
  });
};

export const useCheckout = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: checkout,
    onSuccess: () => {
      // 结账成功后，将购物车重置为空状态，避免重新请求导致的闪烁
      const emptyCart: Cart = { id: null, totalPrice: 0, cartItems: [] };
      queryClient.setQueryData<Cart>(['cart'], emptyCart);
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
  });
};
