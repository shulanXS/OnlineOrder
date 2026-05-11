package com.cwj.onlineorder.repository;

import com.cwj.onlineorder.entity.CartEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.math.BigDecimal;

/**
 * 购物车 Repository。
 *
 * 提供购物车的 CRUD 操作。
 *
 * 方法说明：
 * - getByCustomerId：根据顾客 ID 查询购物车
 * - updateTotalPrice：更新购物车总价（自定义 SQL，需 @Modifying）
 */
public interface CartRepository extends ListCrudRepository<CartEntity, Long> {

    /**
     * 根据顾客 ID 查询购物车。
     * 返回 null 表示该顾客还没有购物车（应在调用处创建）。
     */
    CartEntity getByCustomerId(Long customerId);

    /**
     * 更新购物车的总价。
     *
     * @param cartId     购物车 ID
     * @param totalPrice 新的总价
     */
    @Modifying
    @Query("UPDATE carts SET total_price = :totalPrice WHERE id = :cartId")
    void updateTotalPrice(Long cartId, BigDecimal totalPrice);
}
