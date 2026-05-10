package com.cwj.onlineorder.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * 餐厅实体。
 * 表示平台上的一个餐厅/商家。
 *
 * 字段说明：
 * - id：主键，自增
 * - name：餐厅名称
 * - address：地址
 * - phone：联系电话
 * - imageUrl：餐厅封面图片 URL
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
