-- 分销商后台角色和商城权限菜单
-- 可重复执行：补齐 sys_role/sys_user 业务归属字段、mall:* 权限菜单、DISTRIBUTOR_ADMIN 角色。

SET @db_name := DATABASE();

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

SET @sql := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE sys_user ADD COLUMN account_type VARCHAR(20) DEFAULT ''PLATFORM'' COMMENT ''账号类型: PLATFORM/DISTRIBUTOR/MERCHANT''',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = @db_name AND table_name = 'sys_user' AND column_name = 'account_type'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE sys_user ADD COLUMN distributor_id BIGINT DEFAULT NULL COMMENT ''绑定分销商ID''',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = @db_name AND table_name = 'sys_user' AND column_name = 'distributor_id'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE sys_user ADD COLUMN merchant_id BIGINT DEFAULT NULL COMMENT ''绑定商家ID''',
    'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = @db_name AND table_name = 'sys_user' AND column_name = 'merchant_id'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

INSERT INTO sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
(3000, '商城业务', 0, 20, 'mall-business', NULL, '', '', 1, 0, 'M', '0', '0', '', 'shopping', 'system', NOW(), '商城业务权限根节点'),
(3001, '商家管理', 3000, 1, 'merchant', NULL, '', '', 1, 0, 'C', '0', '0', 'mall:merchant:list', 'peoples', 'system', NOW(), ''),
(3002, '商家查询', 3001, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'mall:merchant:query', '#', 'system', NOW(), ''),
(3003, '商家新增', 3001, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'mall:merchant:add', '#', 'system', NOW(), ''),
(3004, '商家修改', 3001, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'mall:merchant:edit', '#', 'system', NOW(), ''),
(3005, '商家删除', 3001, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'mall:merchant:remove', '#', 'system', NOW(), ''),
(3010, '订单管理', 3000, 2, 'order', NULL, '', '', 1, 0, 'C', '0', '0', 'mall:order:list', 'order', 'system', NOW(), ''),
(3011, '订单查询', 3010, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'mall:order:query', '#', 'system', NOW(), ''),
(3012, '订单修改', 3010, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'mall:order:edit', '#', 'system', NOW(), ''),
(3020, '财务管理', 3000, 3, 'finance', NULL, '', '', 1, 0, 'C', '0', '0', 'mall:finance:list', 'money', 'system', NOW(), ''),
(3021, '财务审批', 3020, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'mall:finance:edit', '#', 'system', NOW(), ''),
(3022, '结算管理', 3020, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'mall:settlement:list', '#', 'system', NOW(), ''),
(3023, '结算操作', 3020, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'mall:settlement:edit', '#', 'system', NOW(), ''),
(3030, '数据分析', 3000, 4, 'data-analysis', NULL, '', '', 1, 0, 'C', '0', '0', 'mall:dashboard:list', 'chart', 'system', NOW(), ''),
(3040, '商品管理', 3000, 5, 'product', NULL, '', '', 1, 0, 'C', '0', '0', 'mall:product:list', 'shopping', 'system', NOW(), ''),
(3041, '商品查询', 3040, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'mall:product:query', '#', 'system', NOW(), ''),
(3042, '商品新增', 3040, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'mall:product:add', '#', 'system', NOW(), ''),
(3043, '商品修改', 3040, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'mall:product:edit', '#', 'system', NOW(), ''),
(3044, '商品删除', 3040, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'mall:product:remove', '#', 'system', NOW(), ''),
(3050, '团购管理', 3000, 6, 'groupon', NULL, '', '', 1, 0, 'C', '0', '0', 'mall:groupon:list', 'component', 'system', NOW(), ''),
(3051, '团购查询', 3050, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'mall:groupon:query', '#', 'system', NOW(), ''),
(3052, '团购新增', 3050, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'mall:groupon:add', '#', 'system', NOW(), ''),
(3053, '团购修改', 3050, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'mall:groupon:edit', '#', 'system', NOW(), ''),
(3054, '团购删除', 3050, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'mall:groupon:remove', '#', 'system', NOW(), ''),
(3090, '分销商管理', 3000, 90, 'distributor', NULL, '', '', 1, 0, 'C', '0', '0', 'mall:distributor:list', 'peoples', 'system', NOW(), '平台专属，不分配给分销商角色'),
(3091, '分销商查询', 3090, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'mall:distributor:query', '#', 'system', NOW(), ''),
(3092, '分销商新增', 3090, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'mall:distributor:add', '#', 'system', NOW(), ''),
(3093, '分销商修改', 3090, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'mall:distributor:edit', '#', 'system', NOW(), ''),
(3094, '分销商删除', 3090, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'mall:distributor:remove', '#', 'system', NOW(), ''),
(3095, '分销商状态', 3090, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'mall:distributor:status', '#', 'system', NOW(), ''),
(3096, '重置分销商密码', 3090, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'mall:distributor:resetPwd', '#', 'system', NOW(), ''),
(3097, '切换分销商视角', 3090, 7, '#', '', '', '', 1, 0, 'F', '0', '0', 'mall:distributor:switch', '#', 'system', NOW(), '')
ON DUPLICATE KEY UPDATE
  menu_name = VALUES(menu_name),
  parent_id = VALUES(parent_id),
  order_num = VALUES(order_num),
  path = VALUES(path),
  menu_type = VALUES(menu_type),
  visible = VALUES(visible),
  status = VALUES(status),
  perms = VALUES(perms),
  icon = VALUES(icon),
  update_by = 'system',
  update_time = NOW();

INSERT INTO sys_role
(role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, role_scope, data_scope_type, remark)
SELECT '分销商管理员', 'DISTRIBUTOR_ADMIN', 10, '5', 1, 1, '0', '0', 'system', NOW(), 'DISTRIBUTOR', 'DISTRIBUTOR_SELF', '分销商后台默认角色'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key = 'DISTRIBUTOR_ADMIN' AND del_flag = '0');

UPDATE sys_role
SET role_name = '分销商管理员',
    role_scope = 'DISTRIBUTOR',
    data_scope = '5',
    data_scope_type = 'DISTRIBUTOR_SELF',
    status = '0',
    update_by = 'system',
    update_time = NOW()
WHERE role_key = 'DISTRIBUTOR_ADMIN' AND del_flag = '0';

DELETE rm
FROM sys_role_menu rm
JOIN sys_role r ON r.role_id = rm.role_id
JOIN sys_menu m ON m.menu_id = rm.menu_id
WHERE r.role_key = 'DISTRIBUTOR_ADMIN'
  AND r.del_flag = '0'
  AND (
    m.perms LIKE 'system:%'
    OR m.perms LIKE 'monitor:%'
    OR m.perms LIKE 'tool:%'
    OR m.perms LIKE 'mall:distributor:%'
  );

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.perms IN (
  'mall:merchant:list',
  'mall:merchant:query',
  'mall:merchant:add',
  'mall:merchant:edit',
  'mall:merchant:remove',
  'mall:order:list',
  'mall:order:query',
  'mall:order:edit',
  'mall:finance:list',
  'mall:finance:edit',
  'mall:settlement:list',
  'mall:settlement:edit',
  'mall:dashboard:list',
  'mall:product:list',
  'mall:product:query',
  'mall:product:add',
  'mall:product:edit',
  'mall:product:remove',
  'mall:groupon:list',
  'mall:groupon:query',
  'mall:groupon:add',
  'mall:groupon:edit',
  'mall:groupon:remove'
)
WHERE r.role_key = 'DISTRIBUTOR_ADMIN' AND r.del_flag = '0';
