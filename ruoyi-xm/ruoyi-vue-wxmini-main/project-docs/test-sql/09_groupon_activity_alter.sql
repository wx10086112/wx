-- ============================================
-- 团购活动表补充字段
-- 执行顺序: 在01之后执行
-- ============================================

USE `ruoyi-cs`;

ALTER TABLE `groupon_activity`
  ADD COLUMN `poster_image` VARCHAR(255) DEFAULT '' COMMENT '活动海报图' AFTER `cover_image`,
  ADD COLUMN `detail_images` TEXT DEFAULT NULL COMMENT '活动详情图JSON数组' AFTER `poster_image`,
  ADD COLUMN `sort` INT DEFAULT 0 COMMENT '排序' AFTER `limit_per_user`,
  ADD COLUMN `source_type` VARCHAR(20) DEFAULT 'ADMIN' COMMENT '创建来源：ADMIN总后台/MERCHANT商家端' AFTER `sort`;
