-- 2026-06-19 WeChat Pay service-provider payment record fields.
-- Run before enabling real partner-mode payments in production.

DROP PROCEDURE IF EXISTS add_payment_record_column_if_missing;
DROP PROCEDURE IF EXISTS add_payment_record_index_if_missing;

DELIMITER //

CREATE PROCEDURE add_payment_record_column_if_missing(IN p_column_name VARCHAR(64), IN p_ddl TEXT)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'payment_record'
      AND column_name = p_column_name
  ) THEN
    SET @sql = CONCAT('ALTER TABLE `payment_record` ADD COLUMN ', p_ddl);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//

CREATE PROCEDURE add_payment_record_index_if_missing(IN p_index_name VARCHAR(64), IN p_ddl TEXT)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'payment_record'
      AND index_name = p_index_name
  ) THEN
    SET @sql = CONCAT('CREATE INDEX `', p_index_name, '` ON `payment_record` ', p_ddl);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//

DELIMITER ;

CALL add_payment_record_column_if_missing('sp_mchid', '`sp_mchid` VARCHAR(64) DEFAULT NULL COMMENT ''service provider mch id'' AFTER `pay_type`');
CALL add_payment_record_column_if_missing('sub_mchid', '`sub_mchid` VARCHAR(64) DEFAULT NULL COMMENT ''sub merchant mch id'' AFTER `sp_mchid`');
CALL add_payment_record_column_if_missing('sub_appid', '`sub_appid` VARCHAR(64) DEFAULT NULL COMMENT ''sub merchant mini appid'' AFTER `sub_mchid`');
CALL add_payment_record_column_if_missing('payer_openid', '`payer_openid` VARCHAR(128) DEFAULT NULL COMMENT ''payer sub_openid'' AFTER `sub_appid`');

CALL add_payment_record_index_if_missing('idx_payment_sp_mchid', '(`sp_mchid`)');
CALL add_payment_record_index_if_missing('idx_payment_sub_mchid', '(`sub_mchid`)');
CALL add_payment_record_index_if_missing('idx_payment_sub_appid', '(`sub_appid`)');
CALL add_payment_record_index_if_missing('idx_payment_payer_openid', '(`payer_openid`)');

DROP PROCEDURE IF EXISTS add_payment_record_column_if_missing;
DROP PROCEDURE IF EXISTS add_payment_record_index_if_missing;
