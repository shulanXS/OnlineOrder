/**
 * useCart Hook.
 * 封装购物车数据的获取和操作。
 */
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getCart, addItemToCart, updateItemQuantity, checkout } from '../api/cartApi';

export const useCart = () => {
  return useQuery({
    queryKey: ['cart'],
    queryFn: getCart,
    staleTime: 30 * 1000,
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
    mutationFn: ({ menuItemId, quantity }) => updateItemQuantity(menuItemId, quantity),
    onMutate: async ({ menuItemId, quantity }) => {
      await queryClient.cancelQueries({ queryKey: ['cart'] });
      const prev = queryClient.getQueryData(['cart']);
      queryClient.setQueryData(['cart'], (old) => {
        if (!old) return old;
        const items = (old.cartItems || []).map(item =>
          item.menuItemId === menuItemId ? { ...item, quantity } : item
        );
        const totalPrice = items.reduce((s, i) => s + Number(i.price) * i.quantity, 0);
        return { ...old, cartItems: items, totalPrice };
      });
      return { prev };
    },
    onError: (e, v, ctx) => {
      if (ctx?.prev) queryClient.setQueryData(['cart'], ctx.prev);
    },
    onSettled: () => queryClient.invalidateQueries({ queryKey: ['cart'] }),
  });
};

export const useCheckout = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: checkout,
    onSuccess: () => {
      queryClient.setQueryData(['cart'], { id: null, totalPrice: 0, cartItems: [] });
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
  });
};
