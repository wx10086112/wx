-- 2026-06-19 Order status history.
-- Run before relying on order timeline/history display.

CREATE TABLE IF NOT EXISTS `mall_order_status_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `order_id` BIGINT DEFAULT NULL COMMENT 'mall_order id',
  `order_no` VARCHAR(64) NOT NULL COMMENT 'order number',
  `merchant_id` BIGINT DEFAULT NULL COMMENT 'merchant id',
  `user_id` BIGINT DEFAULT NULL COMMENT 'user id',
  `from_status` INT DEFAULT NULL COMMENT 'previous order status',
  `to_status` INT NOT NULL COMMENT 'new order status',
  `action` VARCHAR(64) NOT NULL COMMENT 'status change action',
  `source` VARCHAR(64) DEFAULT NULL COMMENT 'change source',
  `operator_id` BIGINT DEFAULT NULL COMMENT 'operator id',
  `operator_name` VARCHAR(100) DEFAULT NULL COMMENT 'operator name',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT 'remark',
  `change_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'change time',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT 'delete flag: 0 exists, 2 deleted',
  PRIMARY KEY (`id`),
  KEY `idx_order_status_history_order_no` (`order_no`),
  KEY `idx_order_status_history_merchant_id` (`merchant_id`),
  KEY `idx_order_status_history_user_id` (`user_id`),
  KEY `idx_order_status_history_change_time` (`change_time`),
  KEY `idx_order_status_history_action` (`action`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='mall order status history';
