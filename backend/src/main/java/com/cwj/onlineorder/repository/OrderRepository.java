package com.cwj.onlineorder.repository;

import com.cwj.onlineorder.entity.OrderEntity;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

/**
 * 订单 Repository。
 *
 * 提供订单的 CRUD 操作。
 *
 * 方法说明：
 * - findByCustomerIdOrderByCreatedAtDesc：查询某用户的所有订单（按创建时间倒序）
 */
public interface OrderRepository extends ListCrudRepository<OrderEntity, Long> {

    /**
     * 查询指定顾客的所有订单（按创建时间倒序）。
     *
     * @param customerId 顾客 ID
     * @return 订单列表
     */
    List<OrderEntity> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
