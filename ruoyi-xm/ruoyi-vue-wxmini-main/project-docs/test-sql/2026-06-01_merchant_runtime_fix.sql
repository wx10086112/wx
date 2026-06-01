USE `ruoyi-cs`;

DROP PROCEDURE IF EXISTS add_merchant_column_if_missing;
DROP PROCEDURE IF EXISTS add_merchant_index_if_missing;

DELIMITER //

CREATE PROCEDURE add_merchant_column_if_missing(IN p_column_name VARCHAR(64), IN p_ddl TEXT)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'merchant'
      AND column_name = p_column_name
  ) THEN
    SET @sql = CONCAT('ALTER TABLE `merchant` ADD COLUMN ', p_ddl);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//

CREATE PROCEDURE add_merchant_index_if_missing(IN p_index_name VARCHAR(64), IN p_ddl TEXT)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'merchant'
      AND index_name = p_index_name
  ) THEN
    SET @sql = CONCAT('CREATE INDEX `', p_index_name, '` ON `merchant` ', p_ddl);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//

DELIMITER ;

CALL add_merchant_column_if_missing('wx_payment_access_type', '`wx_payment_access_type` VARCHAR(32) DEFAULT ''EXISTING_MCH'' COMMENT ''payment access type'' AFTER `wx_applyment_finish_time`');
CALL add_merchant_column_if_missing('merchant_wx_mch_id', '`merchant_wx_mch_id` VARCHAR(64) DEFAULT NULL COMMENT ''merchant wechat pay mch id'' AFTER `wx_payment_access_type`');
CALL add_merchant_column_if_missing('merchant_wx_mch_name', '`merchant_wx_mch_name` VARCHAR(200) DEFAULT NULL COMMENT ''merchant wechat pay mch name'' AFTER `merchant_wx_mch_id`');
CALL add_merchant_column_if_missing('wx_profit_sharing_enabled', '`wx_profit_sharing_enabled` TINYINT DEFAULT 0 COMMENT ''profit sharing enabled'' AFTER `merchant_wx_mch_name`');
CALL add_merchant_column_if_missing('platform_receiver_mch_id', '`platform_receiver_mch_id` VARCHAR(64) DEFAULT NULL COMMENT ''platform receiver mch id'' AFTER `wx_profit_sharing_enabled`');
CALL add_merchant_column_if_missing('distributor_receiver_mch_id', '`distributor_receiver_mch_id` VARCHAR(64) DEFAULT NULL COMMENT ''distributor receiver mch id'' AFTER `platform_receiver_mch_id`');
CALL add_merchant_column_if_missing('merchant_share_rate', '`merchant_share_rate` DECIMAL(5,2) DEFAULT 100.00 COMMENT ''merchant share rate'' AFTER `distributor_receiver_mch_id`');
CALL add_merchant_column_if_missing('platform_share_rate', '`platform_share_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT ''platform share rate'' AFTER `merchant_share_rate`');
CALL add_merchant_column_if_missing('distributor_share_rate', '`distributor_share_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT ''distributor share rate'' AFTER `platform_share_rate`');
CALL add_merchant_column_if_missing('settlement_cycle', '`settlement_cycle` VARCHAR(16) DEFAULT ''T1'' COMMENT ''settlement cycle'' AFTER `distributor_share_rate`');

CALL add_merchant_index_if_missing('idx_merchant_payment_access_type', '(`wx_payment_access_type`)');
CALL add_merchant_index_if_missing('idx_merchant_wx_mch_id', '(`merchant_wx_mch_id`)');
CALL add_merchant_index_if_missing('idx_merchant_profit_sharing_enabled', '(`wx_profit_sharing_enabled`)');

DROP PROCEDURE IF EXISTS add_merchant_column_if_missing;
DROP PROCEDURE IF EXISTS add_merchant_index_if_missing;
