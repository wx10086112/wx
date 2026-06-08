-- Fix missing distributor.del_flag used by current DistributorMapper.
-- Safe to run multiple times.

SET @db_name := DATABASE();

SET @sql := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE distributor ADD COLUMN del_flag CHAR(1) DEFAULT ''0'' COMMENT ''delete flag: 0 exists, 2 deleted'' AFTER update_time',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = @db_name AND table_name = 'distributor' AND column_name = 'del_flag'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE distributor
SET del_flag = '0'
WHERE del_flag IS NULL OR del_flag = '';
