-- Order, settlement, refund and transfer white-box hotfix.
-- This script is rerunnable. Review the preflight result before running it in production.

SELECT 'platform_income duplicate active orders' AS check_item, order_no, del_flag, COUNT(*) AS row_count
FROM platform_income
GROUP BY order_no, del_flag
HAVING COUNT(*) > 1;

SELECT 'merchant_settlement_record duplicate active orders' AS check_item, order_no, del_flag, COUNT(*) AS row_count
FROM merchant_settlement_record
GROUP BY order_no, del_flag
HAVING COUNT(*) > 1;

SELECT 'distributor_settlement_record duplicate active order/distributor pairs' AS check_item,
       order_no, distributor_id, del_flag, COUNT(*) AS row_count
FROM distributor_settlement_record
GROUP BY order_no, distributor_id, del_flag
HAVING COUNT(*) > 1;

DELIMITER $$

DROP PROCEDURE IF EXISTS ensure_finance_column $$
CREATE PROCEDURE ensure_finance_column(
    IN p_table_name VARCHAR(64),
    IN p_column_name VARCHAR(64),
    IN p_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
          AND column_name = p_column_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table_name, '` ADD COLUMN ', p_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$

DROP PROCEDURE IF EXISTS ensure_finance_index $$
CREATE PROCEDURE ensure_finance_index(
    IN p_table_name VARCHAR(64),
    IN p_index_name VARCHAR(64),
    IN p_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
          AND index_name = p_index_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table_name, '` ADD ', p_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$

CALL ensure_finance_column('platform_transfer_record', 'settlement_no',
    '`settlement_no` VARCHAR(64) DEFAULT NULL COMMENT ''linked settlement number''') $$
CALL ensure_finance_column('platform_transfer_record', 'target_type',
    '`target_type` VARCHAR(16) DEFAULT NULL COMMENT ''MERCHANT/DISTRIBUTOR''') $$
CALL ensure_finance_column('platform_transfer_record', 'target_id',
    '`target_id` BIGINT DEFAULT NULL COMMENT ''receiver target id''') $$
CALL ensure_finance_column('platform_transfer_record', 'order_no',
    '`order_no` VARCHAR(64) DEFAULT NULL COMMENT ''linked order number''') $$
CALL ensure_finance_column('platform_transfer_record', 'receiver_openid',
    '`receiver_openid` VARCHAR(128) DEFAULT NULL') $$
CALL ensure_finance_column('platform_transfer_record', 'receiver_name',
    '`receiver_name` VARCHAR(128) DEFAULT NULL') $$
CALL ensure_finance_column('platform_transfer_record', 'receiver_account_type',
    '`receiver_account_type` VARCHAR(32) DEFAULT ''WECHAT_BALANCE''') $$
CALL ensure_finance_column('platform_transfer_record', 'wechat_batch_no',
    '`wechat_batch_no` VARCHAR(128) DEFAULT NULL') $$
CALL ensure_finance_column('platform_transfer_record', 'wechat_detail_no',
    '`wechat_detail_no` VARCHAR(128) DEFAULT NULL') $$
CALL ensure_finance_column('platform_transfer_record', 'apply_time',
    '`apply_time` DATETIME DEFAULT NULL') $$
CALL ensure_finance_column('platform_transfer_record', 'notify_time',
    '`notify_time` DATETIME DEFAULT NULL') $$
CALL ensure_finance_column('platform_transfer_record', 'notify_result',
    '`notify_result` TEXT') $$
CALL ensure_finance_column('platform_transfer_record', 'operator_id',
    '`operator_id` VARCHAR(64) DEFAULT NULL') $$
CALL ensure_finance_column('platform_transfer_record', 'remark',
    '`remark` VARCHAR(500) DEFAULT NULL') $$
CALL ensure_finance_column('platform_transfer_record', 'del_flag',
    '`del_flag` CHAR(1) NOT NULL DEFAULT ''0''') $$
CALL ensure_finance_column('refund_record', 'retry_count',
    '`retry_count` INT NOT NULL DEFAULT 0 COMMENT ''WeChat refund retry attempts''') $$
CALL ensure_finance_column('refund_record', 'last_retry_time',
    '`last_retry_time` DATETIME DEFAULT NULL COMMENT ''latest WeChat refund retry time''') $$
CALL ensure_finance_column('refund_record', 'next_retry_time',
    '`next_retry_time` DATETIME DEFAULT NULL COMMENT ''earliest next WeChat refund retry time''') $$
CALL ensure_finance_column('refund_record', 'last_retry_reason',
    '`last_retry_reason` VARCHAR(500) DEFAULT NULL COMMENT ''latest WeChat refund retry failure reason''') $$

-- Align historic refund states with the Java constants: 1=PENDING, 2=APPROVED,
-- 3=REJECTED, 4=REFUNDED, 5=ABNORMAL. Only zero was an obsolete default.
UPDATE refund_record
SET status = 1
WHERE status = 0
  AND del_flag = '0' $$

-- Migrate the legacy transfer columns when this database still has them.
DROP PROCEDURE IF EXISTS migrate_legacy_platform_transfer $$
CREATE PROCEDURE migrate_legacy_platform_transfer()
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'platform_transfer_record'
          AND column_name = 'settlement_record_type'
    ) THEN
        UPDATE platform_transfer_record ptr
        LEFT JOIN merchant_settlement_record msr
               ON ptr.settlement_record_type = 'MERCHANT'
              AND msr.id = ptr.settlement_record_id
        LEFT JOIN distributor_settlement_record dsr
               ON ptr.settlement_record_type = 'DISTRIBUTOR'
              AND dsr.id = ptr.settlement_record_id
        SET ptr.settlement_no = COALESCE(ptr.settlement_no, msr.settlement_no, dsr.settlement_no),
            ptr.target_type = COALESCE(ptr.target_type, ptr.settlement_record_type),
            ptr.target_id = COALESCE(ptr.target_id,
                CASE ptr.settlement_record_type
                    WHEN 'MERCHANT' THEN ptr.merchant_id
                    WHEN 'DISTRIBUTOR' THEN ptr.distributor_id
                    ELSE NULL
                END),
            ptr.order_no = COALESCE(ptr.order_no, msr.order_no, dsr.order_no),
            ptr.status = CASE ptr.status
                WHEN 'PENDING' THEN 'WAITING'
                WHEN 'SUCCESS' THEN 'ARRIVED'
                ELSE ptr.status
            END;
    END IF;
END $$
CALL migrate_legacy_platform_transfer() $$

CALL ensure_finance_index('platform_transfer_record', 'idx_settlement_no',
    'KEY `idx_settlement_no` (`settlement_no`)') $$
CALL ensure_finance_index('platform_transfer_record', 'idx_target',
    'KEY `idx_target` (`target_type`, `target_id`)') $$
CALL ensure_finance_index('platform_transfer_record', 'idx_wechat_batch_no',
    'KEY `idx_wechat_batch_no` (`wechat_batch_no`)') $$
CALL ensure_finance_index('refund_record', 'idx_refund_retry',
    'KEY `idx_refund_retry` (`status`, `next_retry_time`)') $$

CALL ensure_finance_index('platform_income', 'uk_active_order_no',
    'UNIQUE KEY `uk_active_order_no` (`order_no`, `del_flag`)') $$
CALL ensure_finance_index('merchant_settlement_record', 'uk_active_order_no',
    'UNIQUE KEY `uk_active_order_no` (`order_no`, `del_flag`)') $$
CALL ensure_finance_index('distributor_settlement_record', 'uk_active_order_distributor',
    'UNIQUE KEY `uk_active_order_distributor` (`order_no`, `distributor_id`, `del_flag`)') $$

DROP PROCEDURE IF EXISTS ensure_finance_column $$
DROP PROCEDURE IF EXISTS ensure_finance_index $$
DROP PROCEDURE IF EXISTS migrate_legacy_platform_transfer $$

DELIMITER ;

UPDATE platform_transfer_record
SET status = CASE status
    WHEN 'PENDING' THEN 'WAITING'
    WHEN 'SUCCESS' THEN 'ARRIVED'
    ELSE status
END
WHERE status IN ('PENDING', 'SUCCESS');

ALTER TABLE platform_transfer_record
    MODIFY COLUMN `status` VARCHAR(32) NOT NULL DEFAULT 'WAITING'
    COMMENT 'WAITING/TRANSFERRING/ARRIVED/FAILED/CANCELLED';

ALTER TABLE refund_record
    MODIFY COLUMN `status` TINYINT NOT NULL DEFAULT 1
    COMMENT '1=PENDING,2=APPROVED,3=REJECTED,4=REFUNDED,5=ABNORMAL';

-- This result must be empty before platform transfer is enabled.
SELECT id, transfer_no, settlement_no, target_type, target_id, order_no
FROM platform_transfer_record
WHERE del_flag = '0'
  AND (settlement_no IS NULL OR settlement_no = '' OR target_type IS NULL OR target_id IS NULL);
