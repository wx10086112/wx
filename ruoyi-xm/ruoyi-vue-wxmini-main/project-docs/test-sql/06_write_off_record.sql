-- ============================================
-- 核销记录表
-- 执行顺序: 在01之后执行
-- ============================================

USE `ruoyi-cs`;

DROP TABLE IF EXISTS `write_off_record`;
CREATE TABLE `write_off_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `write_off_code` VARCHAR(32) NOT NULL COMMENT '核销码',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `store_id` BIGINT DEFAULT NULL COMMENT '核销门店ID',
  `operator_id` BIGINT NOT NULL COMMENT '操作员ID(商家用户表ID)',
  `operator_name` VARCHAR(64) DEFAULT '' COMMENT '操作员姓名',
  `write_off_type` TINYINT DEFAULT 1 COMMENT '核销方式: 1扫码核销 2手动核销',
  `write_off_time` DATETIME NOT NULL COMMENT '核销时间',
  `product_name` VARCHAR(200) DEFAULT '' COMMENT '商品名称(冗余)',
  `product_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '商品金额(冗余)',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 1有效 0作废',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_write_off_code` (`write_off_code`),
  UNIQUE KEY `uk_write_off_record_order_no` (`order_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_write_off_time` (`write_off_time`),
  KEY `idx_merchant_time` (`merchant_id`, `write_off_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='核销记录表';
