-- =============================================================================
-- OnlineOrder — V3__rename_order_items_to_cart_items.sql
-- 将 order_items 表重命名为 cart_items（语义更清晰）
--
-- 执行时机：V1, V2 迁移完成后执行
-- =============================================================================

-- 重命名表
ALTER TABLE order_items RENAME TO cart_items;

-- 重命名约束（保持命名一致性）
ALTER INDEX idx_order_items_cart_id RENAME TO idx_cart_items_cart_id;
ALTER INDEX fk_oi_cart RENAME TO fk_ci_cart;
ALTER INDEX fk_oi_menu RENAME TO fk_ci_menu;
