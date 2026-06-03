-- 商家小程序配置唯一索引
-- 作用：
-- 1. 防止同一个 C 端 AppID 被多个商家重复占用
-- 2. 防止同一个商家端 AppID 被多个商家重复占用
-- 注意：
-- - 同一个商家的 c_mini_app_id 和 m_mini_app_id 允许相同
-- - 跨字段（一个商家的 C 端 AppID 与另一个商家的商家端 AppID）冲突，仍需依赖后端业务校验拦截

SET @c_index_exists = (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'merchant'
    AND index_name = 'idx_merchant_c_app_id'
);

SET @c_index_sql = IF(
  @c_index_exists = 0,
  'CREATE UNIQUE INDEX idx_merchant_c_app_id ON merchant(c_mini_app_id)',
  'SELECT ''idx_merchant_c_app_id already exists'''
);

PREPARE c_index_stmt FROM @c_index_sql;
EXECUTE c_index_stmt;
DEALLOCATE PREPARE c_index_stmt;

SET @m_index_exists = (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'merchant'
    AND index_name = 'idx_merchant_m_app_id'
);

SET @m_index_sql = IF(
  @m_index_exists = 0,
  'CREATE UNIQUE INDEX idx_merchant_m_app_id ON merchant(m_mini_app_id)',
  'SELECT ''idx_merchant_m_app_id already exists'''
);

PREPARE m_index_stmt FROM @m_index_sql;
EXECUTE m_index_stmt;
DEALLOCATE PREPARE m_index_stmt;
