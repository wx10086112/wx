USE `ruoyi-cs`;

-- 目的：
-- 修复本地/线上 distributor 表缺少 receiver_openid、receiver_type 字段，
-- 导致 DistributorMapper 查询报 Unknown column。

SET @db_name := DATABASE();

SET @has_receiver_openid := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'distributor'
    AND COLUMN_NAME = 'receiver_openid'
);

SET @sql_receiver_openid := IF(
  @has_receiver_openid = 0,
  'ALTER TABLE distributor ADD COLUMN receiver_openid VARCHAR(128) DEFAULT NULL COMMENT ''收款微信openid'' AFTER status',
  'SELECT ''receiver_openid already exists'' AS message'
);
PREPARE stmt_receiver_openid FROM @sql_receiver_openid;
EXECUTE stmt_receiver_openid;
DEALLOCATE PREPARE stmt_receiver_openid;

SET @has_receiver_type := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'distributor'
    AND COLUMN_NAME = 'receiver_type'
);

SET @sql_receiver_type := IF(
  @has_receiver_type = 0,
  'ALTER TABLE distributor ADD COLUMN receiver_type VARCHAR(32) DEFAULT NULL COMMENT ''收款账户类型'' AFTER receiver_openid',
  'SELECT ''receiver_type already exists'' AS message'
);
PREPARE stmt_receiver_type FROM @sql_receiver_type;
EXECUTE stmt_receiver_type;
DEALLOCATE PREPARE stmt_receiver_type;

SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @db_name
  AND TABLE_NAME = 'distributor'
  AND COLUMN_NAME IN ('receiver_openid', 'receiver_type')
ORDER BY ORDINAL_POSITION;
