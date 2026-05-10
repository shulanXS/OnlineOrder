package com.cwj.onlineorder.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;

/**
 * 订单明细实体。
 * 表示用户结账后一个订单中的具体商品行。
 *
 * 设计要点：
 * - 结账时从购物车的 cart_items 复制而来，是订单的快照。
 * - 每个明细行记录菜品名称、价格、数量等信息，
 *   即使菜品后续被修改或删除，订单记录不受影响。
 *
 * 字段说明：
 * - id：主键，自增
 * - orderId：所属订单 ID（FK）
 * - menuItemId：菜品 ID（快照）
 * - menuItemName：菜品名称快照
 * - menuItemDescription：菜品描述快照
 * - menuItemImageUrl：菜品图片快照
 * - price：购买时的单价快照
 * - quantity：购买数量
 */
@Table("order_details")
public record OrderDetailEntity(
        @Id Long id,
        Long orderId,
        Long menuItemId,
        String menuItemName,
        String menuItemDescription,
        String menuItemImageUrl,
        BigDecimal price,
        Integer quantity
) {
}
