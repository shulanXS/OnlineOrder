package com.cwj.onlineorder.repository;

import com.cwj.onlineorder.entity.CartItemEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import java.util.List;

/**
 * 购物车商品行 Repository。
 *
 * 设计要点：
 * - 购物车商品行通过 (cart_id, menu_item_id) 组合唯一标识。
 *   同一菜品重复添加时更新数量，而非创建重复行。
 * - 所有写操作（DELETE、UPDATE）需要 @Modifying 注解。
 * - 批量删除购物车商品使用单条 SQL 避免 N+1 问题。
 */
public interface CartItemRepository extends ListCrudRepository<CartItemEntity, Long> {

    /**
     * 查询指定购物车的所有商品行。
     */
    List<CartItemEntity> getAllByCartId(Long cartId);

    /**
     * 查询指定购物车中指定菜品的商品行。
     * 用于判断菜品是否已在购物车中（存在则更新数量，否则新增）。
     */
    CartItemEntity findByCartIdAndMenuItemId(Long cartId, Long menuItemId);

    /**
     * 批量查询指定购物车中的所有商品行（按 ID 列表）。
     */
    List<CartItemEntity> findAllByIdIn(List<Long> ids);

    /**
     * 批量删除指定购物车中的所有商品行。
     * 使用单条 SQL 删除所有匹配行，避免逐行删除带来的 N+1 问题。
     */
    @Modifying
    @Query("DELETE FROM cart_items WHERE cart_id = :cartId")
    void deleteByCartId(Long cartId);
}
