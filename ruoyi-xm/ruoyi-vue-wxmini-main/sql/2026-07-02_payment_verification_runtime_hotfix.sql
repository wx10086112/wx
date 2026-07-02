-- 2026-07-02 payment, verification, and settlement runtime hotfix.
-- Idempotent: safe to run multiple times on local or production databases.

DROP PROCEDURE IF EXISTS add_column_if_missing;
DROP PROCEDURE IF EXISTS add_index_if_missing;

DELIMITER //

CREATE PROCEDURE add_column_if_missing(IN p_table_name VARCHAR(64), IN p_column_name VARCHAR(64), IN p_ddl TEXT)
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = p_table_name
  ) AND NOT EXISTS (
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
  IF EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = p_table_name
  ) AND NOT EXISTS (
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

CALL add_column_if_missing('mall_order', 'write_off_status',
  '`write_off_status` TINYINT DEFAULT 0 COMMENT ''write off status: 0 unused, 1 used'' AFTER `write_off_code`');
CALL add_column_if_missing('mall_order', 'write_off_time',
  '`write_off_time` DATETIME DEFAULT NULL COMMENT ''write off time'' AFTER `write_off_status`');
CALL add_column_if_missing('mall_order', 'write_off_user_id',
  '`write_off_user_id` BIGINT DEFAULT NULL COMMENT ''write off operator id'' AFTER `write_off_time`');
CALL add_column_if_missing('mall_order', 'valid_days',
  '`valid_days` INT DEFAULT 0 COMMENT ''valid days copied from product'' AFTER `write_off_user_id`');

CALL add_index_if_missing('mall_order', 'idx_write_off_status', '(`write_off_status`)');

CALL add_column_if_missing('order_profit_ledger', 'remark',
  '`remark` VARCHAR(500) DEFAULT NULL COMMENT ''remark'' AFTER `finish_time`');
CALL add_column_if_missing('merchant_settlement_record', 'remark',
  '`remark` VARCHAR(500) DEFAULT NULL COMMENT ''remark'' AFTER `reverse_record_id`');
CALL add_column_if_missing('distributor_settlement_record', 'remark',
  '`remark` VARCHAR(500) DEFAULT NULL COMMENT ''remark'' AFTER `fail_reason`');

DROP PROCEDURE IF EXISTS add_column_if_missing;
DROP PROCEDURE IF EXISTS add_index_if_missing;
