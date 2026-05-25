-- ============================================
-- 商品表补充字段
-- 执行顺序: 在01之后执行
-- ============================================

USE `ruoyi-cs`;

ALTER TABLE `product`
  ADD COLUMN `main_image` VARCHAR(500) DEFAULT NULL COMMENT '商品主图URL(冗余，快速展示用)' AFTER `images`,
  ADD COLUMN `verify_type` TINYINT DEFAULT 1 COMMENT '核销方式: 1在线核销 2到店自提' AFTER `status`,
  ADD INDEX `idx_verify_type` (`verify_type`);
