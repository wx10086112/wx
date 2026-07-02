-- 2026-07-02 merchant contract ratio metadata.
-- Safe to run multiple times before enabling merchant-level profit sharing contracts.

DROP PROCEDURE IF EXISTS add_column_if_missing;

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

DELIMITER ;

CALL add_column_if_missing('merchant', 'profit_sharing_contract_version',
  '`profit_sharing_contract_version` VARCHAR(128) DEFAULT NULL COMMENT ''profit sharing contract version'' AFTER `distributor_share_rate`');

DROP PROCEDURE IF EXISTS add_column_if_missing;
