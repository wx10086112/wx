-- 2026-06-20 service-provider runtime schema hotfix.
-- Safe to run multiple times before enabling real WeChat Pay partner-mode callbacks.

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

DROP PROCEDURE IF EXISTS add_column_if_missing;
DROP PROCEDURE IF EXISTS add_index_if_missing;

DELIMITER //

CREATE PROCEDURE add_column_if_missing(IN p_table_name VARCHAR(64), IN p_column_name VARCHAR(64), IN p_ddl TEXT)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = p_table_name
      AND column_name = p_column_name
  ) THEN
    SET @sql = CONCAT('ALTER TABLE `', p_table_name, '` ADD COLUMN ', p_ddl);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//

CREATE PROCEDURE add_index_if_missing(IN p_table_name VARCHAR(64), IN p_index_name VARCHAR(64), IN p_ddl TEXT)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = p_table_name
      AND index_name = p_index_name
  ) THEN
    SET @sql = CONCAT('CREATE INDEX `', p_index_name, '` ON `', p_table_name, '` ', p_ddl);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//

DELIMITER ;

CALL add_column_if_missing('payment_record', 'sp_mchid',
  '`sp_mchid` VARCHAR(64) DEFAULT NULL COMMENT ''service provider mch id'' AFTER `pay_type`');
CALL add_column_if_missing('payment_record', 'sub_mchid',
  '`sub_mchid` VARCHAR(64) DEFAULT NULL COMMENT ''sub merchant mch id'' AFTER `sp_mchid`');
CALL add_column_if_missing('payment_record', 'sub_appid',
  '`sub_appid` VARCHAR(64) DEFAULT NULL COMMENT ''sub merchant mini appid'' AFTER `sub_mchid`');
CALL add_column_if_missing('payment_record', 'payer_openid',
  '`payer_openid` VARCHAR(128) DEFAULT NULL COMMENT ''payer sub_openid'' AFTER `sub_appid`');

CALL add_index_if_missing('payment_record', 'idx_payment_sp_mchid', '(`sp_mchid`)');
CALL add_index_if_missing('payment_record', 'idx_payment_sub_mchid', '(`sub_mchid`)');
CALL add_index_if_missing('payment_record', 'idx_payment_sub_appid', '(`sub_appid`)');
CALL add_index_if_missing('payment_record', 'idx_payment_payer_openid', '(`payer_openid`)');

CALL add_column_if_missing('merchant', 'wx_pay_mch_id',
  '`wx_pay_mch_id` VARCHAR(64) DEFAULT NULL COMMENT ''wechat merchant id'' AFTER `m_mini_app_secret`');
CALL add_column_if_missing('merchant', 'wx_pay_api_key',
  '`wx_pay_api_key` VARCHAR(128) DEFAULT NULL COMMENT ''wechat pay api key'' AFTER `wx_pay_mch_id`');
CALL add_column_if_missing('merchant', 'receiver_openid',
  '`receiver_openid` VARCHAR(128) DEFAULT NULL COMMENT ''receiver openid'' AFTER `wx_pay_api_key`');
CALL add_column_if_missing('merchant', 'receiver_type',
  '`receiver_type` VARCHAR(32) DEFAULT ''WECHAT_BALANCE'' COMMENT ''receiver account type'' AFTER `receiver_openid`');
CALL add_column_if_missing('merchant', 'map_claim_status',
  '`map_claim_status` VARCHAR(32) DEFAULT ''NOT_CLAIMED'' COMMENT ''map claim status'' AFTER `receiver_type`');
CALL add_column_if_missing('merchant', 'map_poi_id',
  '`map_poi_id` VARCHAR(128) DEFAULT NULL COMMENT ''map poi id'' AFTER `map_claim_status`');
CALL add_column_if_missing('merchant', 'map_claim_url',
  '`map_claim_url` VARCHAR(500) DEFAULT NULL COMMENT ''map claim url'' AFTER `map_poi_id`');
CALL add_column_if_missing('merchant', 'map_claim_time',
  '`map_claim_time` DATETIME DEFAULT NULL COMMENT ''map claim time'' AFTER `map_claim_url`');
CALL add_column_if_missing('merchant', 'map_claim_remark',
  '`map_claim_remark` VARCHAR(500) DEFAULT NULL COMMENT ''map claim remark'' AFTER `map_claim_time`');
CALL add_column_if_missing('merchant', 'wx_applyment_id',
  '`wx_applyment_id` VARCHAR(128) DEFAULT NULL COMMENT ''wechat applyment id'' AFTER `map_claim_remark`');
CALL add_column_if_missing('merchant', 'wx_applyment_state',
  '`wx_applyment_state` VARCHAR(64) DEFAULT ''NOT_SUBMITTED'' COMMENT ''wechat applyment state'' AFTER `wx_applyment_id`');
CALL add_column_if_missing('merchant', 'wx_applyment_reject_reason',
  '`wx_applyment_reject_reason` VARCHAR(1000) DEFAULT NULL COMMENT ''wechat applyment reject reason'' AFTER `wx_applyment_state`');
CALL add_column_if_missing('merchant', 'wx_applyment_time',
  '`wx_applyment_time` DATETIME DEFAULT NULL COMMENT ''wechat applyment time'' AFTER `wx_applyment_reject_reason`');
CALL add_column_if_missing('merchant', 'wx_applyment_finish_time',
  '`wx_applyment_finish_time` DATETIME DEFAULT NULL COMMENT ''wechat applyment finish time'' AFTER `wx_applyment_time`');
CALL add_column_if_missing('merchant', 'wx_payment_access_type',
  '`wx_payment_access_type` VARCHAR(32) DEFAULT ''EXISTING_MCH'' COMMENT ''wechat pay access type'' AFTER `wx_applyment_finish_time`');
CALL add_column_if_missing('merchant', 'merchant_wx_mch_id',
  '`merchant_wx_mch_id` VARCHAR(64) DEFAULT NULL COMMENT ''sub merchant mch id'' AFTER `wx_payment_access_type`');
CALL add_column_if_missing('merchant', 'merchant_wx_mch_name',
  '`merchant_wx_mch_name` VARCHAR(200) DEFAULT NULL COMMENT ''sub merchant mch name'' AFTER `merchant_wx_mch_id`');
CALL add_column_if_missing('merchant', 'wx_profit_sharing_enabled',
  '`wx_profit_sharing_enabled` TINYINT DEFAULT 0 COMMENT ''wechat profit sharing enabled'' AFTER `merchant_wx_mch_name`');
CALL add_column_if_missing('merchant', 'platform_receiver_mch_id',
  '`platform_receiver_mch_id` VARCHAR(64) DEFAULT NULL COMMENT ''platform receiver mch id'' AFTER `wx_profit_sharing_enabled`');
CALL add_column_if_missing('merchant', 'distributor_receiver_mch_id',
  '`distributor_receiver_mch_id` VARCHAR(64) DEFAULT NULL COMMENT ''distributor receiver mch id'' AFTER `platform_receiver_mch_id`');
CALL add_column_if_missing('merchant', 'merchant_share_rate',
  '`merchant_share_rate` DECIMAL(5,2) DEFAULT 100.00 COMMENT ''merchant share rate percent'' AFTER `distributor_receiver_mch_id`');
CALL add_column_if_missing('merchant', 'platform_share_rate',
  '`platform_share_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT ''platform share rate percent'' AFTER `merchant_share_rate`');
CALL add_column_if_missing('merchant', 'distributor_share_rate',
  '`distributor_share_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT ''distributor share rate percent'' AFTER `platform_share_rate`');
CALL add_column_if_missing('merchant', 'settlement_cycle',
  '`settlement_cycle` VARCHAR(16) DEFAULT ''T1'' COMMENT ''settlement cycle'' AFTER `distributor_share_rate`');

CALL add_index_if_missing('merchant', 'idx_merchant_wx_applyment_id', '(`wx_applyment_id`)');
CALL add_index_if_missing('merchant', 'idx_merchant_map_claim_status', '(`map_claim_status`)');
CALL add_index_if_missing('merchant', 'idx_merchant_wx_applyment_state', '(`wx_applyment_state`)');
CALL add_index_if_missing('merchant', 'idx_merchant_payment_access_type', '(`wx_payment_access_type`)');
CALL add_index_if_missing('merchant', 'idx_merchant_wx_mch_id', '(`merchant_wx_mch_id`)');
CALL add_index_if_missing('merchant', 'idx_merchant_profit_sharing_enabled', '(`wx_profit_sharing_enabled`)');

CALL add_column_if_missing('merchant_settlement_record', 'remark',
  '`remark` VARCHAR(500) DEFAULT NULL COMMENT ''remark'' AFTER `reverse_record_id`');
CALL add_column_if_missing('order_profit_ledger', 'remark',
  '`remark` VARCHAR(500) DEFAULT NULL COMMENT ''remark'' AFTER `finish_time`');
CALL add_column_if_missing('distributor_settlement_record', 'remark',
  '`remark` VARCHAR(500) DEFAULT NULL COMMENT ''remark'' AFTER `fail_reason`');

DROP PROCEDURE IF EXISTS add_column_if_missing;
DROP PROCEDURE IF EXISTS add_index_if_missing;
