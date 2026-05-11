package com.cwj.onlineorder.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * 餐厅实体。
 *
 * 表示平台上的一个餐厅/商家。
 *
 * 字段说明：
 * - id：主键，自增（BIGSERIAL）
 * - name：餐厅名称
 * - address：地址
 * - phone：联系电话
 * - imageUrl：餐厅封面图片 URL（可以是相对路径或绝对 URL）
 *
 * @see com.cwj.onlineorder.repository.RestaurantRepository
 * @see com.cwj.onlineorder.service.RestaurantService
 */
@Table("restaurants")
public record RestaurantEntity(
        @Id Long id,
        String name,
        String address,
        String phone,
        String imageUrl
) {
}
