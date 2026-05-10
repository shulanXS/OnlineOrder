package com.cwj.onlineorder.repository;

import com.cwj.onlineorder.entity.CartEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.math.BigDecimal;

public interface CartRepository extends ListCrudRepository<CartEntity, Long> {

    CartEntity getByCustomerId(Long customerId);

    @Modifying
    @Query("UPDATE carts SET total_price = :totalPrice WHERE id = :cartId")
    void updateTotalPrice(Long cartId, BigDecimal totalPrice);
}
