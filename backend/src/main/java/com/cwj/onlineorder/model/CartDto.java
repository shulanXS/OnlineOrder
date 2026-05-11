package com.cwj.onlineorder.model;

import com.cwj.onlineorder.entity.CartEntity;
import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车数据传输对象。
 *
 * 返回给前端的购物车完整结构，包含：
 * - 购物车 ID
 * - 总价（快照，结账时锁定）
 * - 商品行列表（含菜品详情）
 *
 * 序列化命名：
 * - Jackson 使用默认 camelCase 命名（application.yaml 中未设置命名策略）
 * - JSON 字段如 { "id": 1, "totalPrice": 10.59, "cartItems": [...] }
 * - 前端 TypeScript Cart 接口字段名必须与 camelCase 一致
 *
 * @param id         购物车 ID
 * @param totalPrice 购物车总价（BigDecimal -> JSON number）
 * @param cartItems  商品行列表
 */
public record CartDto(
        Long id,
        BigDecimal totalPrice,
        List<CartItemDto> cartItems
) {
    /**
     * 从实体构建 DTO。
     *
     * @param entity   购物车实体
     * @param items    商品行 DTO 列表
     */
    public CartDto(CartEntity entity, List<CartItemDto> items) {
        this(entity.id(), entity.totalPrice(), items);
    }
}
