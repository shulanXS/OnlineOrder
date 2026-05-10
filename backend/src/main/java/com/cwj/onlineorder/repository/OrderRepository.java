package com.cwj.onlineorder.repository;

import com.cwj.onlineorder.entity.OrderEntity;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface OrderRepository extends ListCrudRepository<OrderEntity, Long> {

    List<OrderEntity> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
