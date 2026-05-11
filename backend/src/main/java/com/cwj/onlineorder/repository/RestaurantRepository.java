package com.cwj.onlineorder.repository;

import com.cwj.onlineorder.entity.RestaurantEntity;
import org.springframework.data.repository.ListCrudRepository;

/**
 * 餐厅数据访问层。
 *
 * 基于 Spring Data JDBC，findAll() 返回所有餐厅。
 * 菜品查询由 MenuItemRepository 提供。
 */
public interface RestaurantRepository extends ListCrudRepository<RestaurantEntity, Long> {
}
