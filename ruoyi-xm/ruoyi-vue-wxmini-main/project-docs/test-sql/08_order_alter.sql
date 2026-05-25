-- ============================================
-- 订单表补充核销字段
-- 执行顺序: 在01之后执行
-- ============================================

USE `ruoyi-cs`;

ALTER TABLE `mall_order`
  ADD COLUMN `write_off_status` TINYINT DEFAULT 0 COMMENT '核销状态: 0未核销 1已核销' AFTER `write_off_code`,
  ADD COLUMN `write_off_time` DATETIME DEFAULT NULL COMMENT '核销时间' AFTER `write_off_status`,
  ADD COLUMN `write_off_user_id` BIGINT DEFAULT NULL COMMENT '核销操作员ID' AFTER `write_off_time`,
  ADD COLUMN `valid_days` INT DEFAULT 0 COMMENT '商品有效天数(冗余自商品表)' AFTER `write_off_user_id`,
  ADD INDEX `idx_write_off_status` (`write_off_status`);
