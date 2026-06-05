-- Fix missing user/role business-scope columns used by current backend mappers.
-- Safe to run multiple times.

SET @db_name := DATABASE();

SET @sql := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE sys_user ADD COLUMN account_type VARCHAR(20) DEFAULT ''PLATFORM'' COMMENT ''账号类型: PLATFORM/DISTRIBUTOR/MERCHANT'' AFTER remark',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = @db_name AND table_name = 'sys_user' AND column_name = 'account_type'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE sys_user ADD COLUMN distributor_id BIGINT DEFAULT NULL COMMENT ''绑定分销商ID'' AFTER account_type',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = @db_name AND table_name = 'sys_user' AND column_name = 'distributor_id'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE sys_user ADD COLUMN merchant_id BIGINT DEFAULT NULL COMMENT ''绑定商家ID'' AFTER distributor_id',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = @db_name AND table_name = 'sys_user' AND column_name = 'merchant_id'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE sys_role ADD COLUMN role_scope VARCHAR(20) DEFAULT ''PLATFORM'' COMMENT ''角色归属: PLATFORM/DISTRIBUTOR/MERCHANT''',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = @db_name AND table_name = 'sys_role' AND column_name = 'role_scope'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE sys_role ADD COLUMN data_scope_type VARCHAR(30) DEFAULT NULL COMMENT ''业务数据范围: ALL/DISTRIBUTOR_SELF/DISTRIBUTOR_CUSTOM/MERCHANT_SELF/MERCHANT_CUSTOM''',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = @db_name AND table_name = 'sys_role' AND column_name = 'data_scope_type'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE sys_role ADD COLUMN distributor_id BIGINT DEFAULT NULL COMMENT ''绑定分销商ID''',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = @db_name AND table_name = 'sys_role' AND column_name = 'distributor_id'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE sys_user
SET account_type = 'PLATFORM'
WHERE account_type IS NULL OR account_type = '';
