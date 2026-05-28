-- ============================================================
-- P0-8: 核心业务表增加逻辑删除 del_flag
-- P0-7: 金额单位确认（DECIMAL 元，接口层统一乘100转分返回）
-- ============================================================

-- 一、增加 del_flag 列
ALTER TABLE `merchant` ADD COLUMN `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）';
ALTER TABLE `merchant_user` ADD COLUMN `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）';
ALTER TABLE `merchant_store` ADD COLUMN `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）';
ALTER TABLE `product` ADD COLUMN `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）';
ALTER TABLE `product_category` ADD COLUMN `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）';
ALTER TABLE `mall_order` ADD COLUMN `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）';
ALTER TABLE `order_item` ADD COLUMN `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）';
ALTER TABLE `refund_record` ADD COLUMN `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）';
ALTER TABLE `withdraw_record` ADD COLUMN `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）';
ALTER TABLE `groupon_activity` ADD COLUMN `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）';
ALTER TABLE `groupon_activity_item` ADD COLUMN `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）';
ALTER TABLE `coupon` ADD COLUMN `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）';
ALTER TABLE `payment_record` ADD COLUMN `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）';

-- 二、groupon_activity_item 金额列统一为 DECIMAL(10,2) 元
-- 注意：建表SQL(10_groupon_activity_item.sql)已改为 DECIMAL(10,2)
-- 如已有 BIGINT 列的旧库，需执行以下 ALTER：
-- ALTER TABLE `groupon_activity_item` MODIFY `original_price` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '原价，单位元';
-- ALTER TABLE `groupon_activity_item` MODIFY `groupon_price` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '团购价/现价，单位元';

-- 三、order_item 增加商家维度索引（P2项）
ALTER TABLE `order_item` ADD KEY `idx_merchant_id` (`merchant_id`);

-- 三、库存条件更新（P2项，仅确保SQL正确性，实际在Mapper中修改）
-- UPDATE product SET stock = stock - #{quantity} WHERE id = #{id} AND stock >= #{quantity}
