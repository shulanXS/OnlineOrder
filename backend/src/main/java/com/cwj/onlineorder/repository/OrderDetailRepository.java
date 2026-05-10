package com.cwj.onlineorder.repository;

import com.cwj.onlineorder.entity.OrderDetailEntity;
import org.springframework.data.repository.ListCrudRepository;
import java.util.List;

/**
 * 订单明细 Repository。
 */
public interface OrderDetailRepository extends ListCrudRepository<OrderDetailEntity, Long> {

    /**
     * 查询指定订单的所有明细。
     */
    List<OrderDetailEntity> findByOrderId(Long orderId);

    /**
     * 批量查询指定订单 ID 列表的所有订单明细。
     * 用于一次性加载多个订单的明细，避免 N+1 查询问题。
     */
    List<OrderDetailEntity> findAllByOrderIdIn(List<Long> orderIds);
}
