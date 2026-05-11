package com.cwj.onlineorder.repository;

import com.cwj.onlineorder.entity.OrderDetailEntity;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

/**
 * 订单明细 Repository。
 *
 * 提供订单明细的 CRUD 操作。
 *
 * 方法说明：
 * - findByOrderId：查询指定订单的所有明细
 * - findAllByOrderIdIn：批量查询多个订单的明细（用于优化 N+1 查询）
 */
public interface OrderDetailRepository extends ListCrudRepository<OrderDetailEntity, Long> {

    /**
     * 查询指定订单的所有明细。
     *
     * @param orderId 订单 ID
     * @return 该订单的所有明细（按 ID 升序）
     */
    List<OrderDetailEntity> findByOrderId(Long orderId);

    /**
     * 批量查询多个订单的所有明细。
     *
     * @param orderIds 订单 ID 列表
     * @return 所有相关订单的明细
     */
    List<OrderDetailEntity> findAllByOrderIdIn(List<Long> orderIds);
}
