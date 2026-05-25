-- ============================================
-- 商品图片表
-- 执行顺序: 在01之后执行
-- ============================================

USE `ruoyi-cs`;

DROP TABLE IF EXISTS `product_image`;
CREATE TABLE `product_image` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `image_type` VARCHAR(20) NOT NULL COMMENT '图片类型: main主图/detail详情图/sku',
  `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL路径',
  `sort_order` INT DEFAULT 0 COMMENT '排序号(越小越靠前)',
  `sku_value` VARCHAR(100) DEFAULT NULL COMMENT 'SKU值(如: 红色/蓝色, 仅sku类型时填写)',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0删除 1正常',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_product_type` (`product_id`, `image_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品图片表';
