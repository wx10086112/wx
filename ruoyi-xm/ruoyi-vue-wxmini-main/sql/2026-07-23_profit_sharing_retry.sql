-- WeChat profit-sharing retry and reconciliation fields.
-- This migration is rerunnable and must run before deploying the matching JAR.

DELIMITER $$

DROP PROCEDURE IF EXISTS ensure_profit_sharing_ledger_column $$
CREATE PROCEDURE ensure_profit_sharing_ledger_column(
    IN p_column_name VARCHAR(64),
    IN p_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'order_profit_ledger'
          AND column_name = p_column_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `order_profit_ledger` ADD COLUMN ', p_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$

DROP PROCEDURE IF EXISTS ensure_profit_sharing_ledger_index $$
CREATE PROCEDURE ensure_profit_sharing_ledger_index(
    IN p_index_name VARCHAR(64),
    IN p_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'order_profit_ledger'
          AND index_name = p_index_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `order_profit_ledger` ADD ', p_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$

CALL ensure_profit_sharing_ledger_column('profit_sharing_attempts',
    '`profit_sharing_attempts` INT NOT NULL DEFAULT 0 COMMENT ''WeChat profit-sharing attempts'' AFTER `status`') $$
CALL ensure_profit_sharing_ledger_column('profit_sharing_last_attempt_time',
    '`profit_sharing_last_attempt_time` DATETIME DEFAULT NULL COMMENT ''latest WeChat profit-sharing attempt'' AFTER `profit_sharing_attempts`') $$
CALL ensure_profit_sharing_ledger_column('profit_sharing_next_retry_time',
    '`profit_sharing_next_retry_time` DATETIME DEFAULT NULL COMMENT ''earliest next WeChat profit-sharing retry'' AFTER `profit_sharing_last_attempt_time`') $$
CALL ensure_profit_sharing_ledger_column('profit_sharing_out_order_no',
    '`profit_sharing_out_order_no` VARCHAR(64) DEFAULT NULL COMMENT ''merchant profit-sharing order number'' AFTER `profit_sharing_next_retry_time`') $$
CALL ensure_profit_sharing_ledger_column('profit_sharing_order_id',
    '`profit_sharing_order_id` VARCHAR(64) DEFAULT NULL COMMENT ''WeChat profit-sharing order id'' AFTER `profit_sharing_out_order_no`') $$

CALL ensure_profit_sharing_ledger_index('idx_profit_sharing_retry',
    'KEY `idx_profit_sharing_retry` (`status`, `profit_sharing_next_retry_time`)') $$

DROP PROCEDURE IF EXISTS ensure_profit_sharing_ledger_column $$
DROP PROCEDURE IF EXISTS ensure_profit_sharing_ledger_index $$

DELIMITER ;
