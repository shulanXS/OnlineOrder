/**
 * useCart Hook.
 * 封装购物车数据的获取和操作。
 */
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getCart, addItemToCart, updateItemQuantity, checkout } from '../api/cartApi';
import { CART_STALE_TIME } from '../constants';

export const useCart = () => {
  return useQuery({
    queryKey: ['cart'],
    queryFn: getCart,
    staleTime: CART_STALE_TIME,
    refetchOnMount: true,
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
      const prev = queryClient.getQueryData(['cart']);
      queryClient.setQueryData(['cart'], (old: unknown) => {
        if (!old) return old;
        const oldCart = old as { cartItems?: Array<{ menuItemId: number; quantity: number; price: number }>; totalPrice?: number };
        const items = (oldCart.cartItems || []).map((item) =>
          item.menuItemId === menuItemId ? { ...item, quantity } : item
        );
        const totalPrice = items.reduce((s, i) => s + Number(i.price) * i.quantity, 0);
        return { ...oldCart, cartItems: items, totalPrice };
      });
      return { prev };
    },
    onError: (_e, _v, ctx) => {
      if (ctx?.prev) {
        queryClient.setQueryData(['cart'], ctx.prev);
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
      // 结账成功后，将购物车 query data 重置为空购物车状态（不显示加载状态）
      const emptyCart: { id: null; totalPrice: number; cartItems: never[] } = {
        id: null,
        totalPrice: 0,
        cartItems: [],
      };
      queryClient.setQueryData(['cart'], emptyCart);
      // 刷新订单列表
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
  });
};
