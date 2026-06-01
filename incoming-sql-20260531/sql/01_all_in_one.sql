-- ============================================
-- 零点科技多商家SaaS团购商城 - 服务器完整建表
-- 数据库: ruoyi-cs
-- 包含: RuoYi框架表 + Quartz + 全部30张业务表 + SaaS字段
-- 执行顺序：从头到尾执行即可
-- ============================================

USE `ruoyi-cs`;

-- ============================================================
-- 第一部分: RuoYi系统框架表(含SaaS扩展字段)
-- ============================================================

-- 1. sys_user 系统用户表（含 SaaS 账号类型字段）
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `dept_id` bigint(20) DEFAULT NULL COMMENT '部门ID',
  `user_name` varchar(30) NOT NULL COMMENT '用户账号',
  `nick_name` varchar(30) NOT NULL COMMENT '用户昵称',
  `user_type` varchar(2) DEFAULT '00' COMMENT '用户类型（00系统用户）',
  `email` varchar(50) DEFAULT '' COMMENT '用户邮箱',
  `phonenumber` varchar(11) DEFAULT '' COMMENT '手机号码',
  `sex` char(1) DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
  `avatar` varchar(100) DEFAULT '' COMMENT '头像路径',
  `password` varchar(100) DEFAULT '' COMMENT '密码',
  `status` char(1) DEFAULT '0' COMMENT '帐号状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `login_ip` varchar(128) DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `account_type` varchar(32) DEFAULT 'PLATFORM' COMMENT '账号类型：SUPER_ADMIN/DISTRIBUTOR_ADMIN/MERCHANT_ADMIN/MERCHANT_STAFF',
  `distributor_id` bigint(20) DEFAULT NULL COMMENT '所属分销商ID',
  `merchant_id` bigint(20) DEFAULT NULL COMMENT '所属商家ID',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='用户信息表';

-- 2. sys_dept
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
  `dept_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '部门id',
  `parent_id` bigint(20) DEFAULT '0' COMMENT '父部门id',
  `ancestors` varchar(50) DEFAULT '' COMMENT '祖级列表',
  `dept_name` varchar(30) DEFAULT '' COMMENT '部门名称',
  `order_num` int(4) DEFAULT '0' COMMENT '显示顺序',
  `leader` varchar(20) DEFAULT NULL COMMENT '负责人',
  `phone` varchar(11) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
  `status` char(1) DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`dept_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=200 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='部门表';

-- 3. sys_role
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `role_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(30) NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) NOT NULL COMMENT '角色权限字符串',
  `role_sort` int(4) NOT NULL COMMENT '显示顺序',
  `data_scope` char(1) DEFAULT '1' COMMENT '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限 5：仅本人数据权限）',
  `menu_check_strictly` tinyint(1) DEFAULT '1' COMMENT '菜单树选择项是否关联显示',
  `dept_check_strictly` tinyint(1) DEFAULT '1' COMMENT '部门树选择项是否关联显示',
  `status` char(1) NOT NULL COMMENT '角色状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='角色信息表';

-- 4. sys_menu
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `menu_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `menu_name` varchar(50) NOT NULL COMMENT '菜单名称',
  `parent_id` bigint(20) DEFAULT '0' COMMENT '父菜单ID',
  `order_num` int(4) DEFAULT '0' COMMENT '显示顺序',
  `path` varchar(200) DEFAULT '' COMMENT '路由地址',
  `component` varchar(255) DEFAULT NULL COMMENT '组件路径',
  `query` varchar(255) DEFAULT NULL COMMENT '路由参数',
  `is_frame` int(1) DEFAULT '1' COMMENT '是否为外链（0是 1否）',
  `is_cache` int(1) DEFAULT '0' COMMENT '是否缓存（0缓存 1不缓存）',
  `menu_type` char(1) DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
  `visible` char(1) DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
  `status` char(1) DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
  `perms` varchar(100) DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) DEFAULT '#' COMMENT '菜单图标',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`menu_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2000 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='菜单权限表';

-- 5. sys_user_role
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`,`role_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='用户和角色关联表';

-- 6. sys_role_menu
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`,`menu_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='角色和菜单关联表';

-- 7. sys_role_dept
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept` (
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `dept_id` bigint(20) NOT NULL COMMENT '部门ID',
  PRIMARY KEY (`role_id`,`dept_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='角色和部门关联表';

-- 8. sys_user_post
DROP TABLE IF EXISTS `sys_user_post`;
CREATE TABLE `sys_user_post` (
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `post_id` bigint(20) NOT NULL COMMENT '岗位ID',
  PRIMARY KEY (`user_id`,`post_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='用户与岗位关联表';

-- 9. sys_post
DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post` (
  `post_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  `post_code` varchar(64) NOT NULL COMMENT '岗位编码',
  `post_name` varchar(50) NOT NULL COMMENT '岗位名称',
  `post_sort` int(4) NOT NULL COMMENT '显示顺序',
  `status` char(1) NOT NULL COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`post_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='岗位信息表';

-- 10. sys_dict_type
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type` (
  `dict_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  `dict_name` varchar(100) DEFAULT '' COMMENT '字典名称',
  `dict_type` varchar(100) DEFAULT '' COMMENT '字典类型',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='字典类型表';

-- 11. sys_dict_data
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
  `dict_code` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '字典编码',
  `dict_sort` int(4) DEFAULT '0' COMMENT '字典排序',
  `dict_label` varchar(100) DEFAULT '' COMMENT '字典标签',
  `dict_value` varchar(100) DEFAULT '' COMMENT '字典键值',
  `dict_type` varchar(100) DEFAULT '' COMMENT '字典类型',
  `css_class` varchar(100) DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
  `list_class` varchar(100) DEFAULT NULL COMMENT '表格回显样式',
  `is_default` char(1) DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='字典数据表';

-- 12. sys_config
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `config_id` int(5) NOT NULL AUTO_INCREMENT COMMENT '参数主键',
  `config_name` varchar(100) DEFAULT '' COMMENT '参数名称',
  `config_key` varchar(100) DEFAULT '' COMMENT '参数键名',
  `config_value` varchar(500) DEFAULT '' COMMENT '参数键值',
  `config_type` char(1) DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`config_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='参数配置表';

-- 13. sys_oper_log
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log` (
  `oper_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) DEFAULT '' COMMENT '模块标题',
  `business_type` int(2) DEFAULT '0' COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `method` varchar(100) DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) DEFAULT '' COMMENT '请求方式',
  `operator_type` int(1) DEFAULT '0' COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
  `oper_name` varchar(50) DEFAULT '' COMMENT '操作人员',
  `dept_name` varchar(50) DEFAULT '' COMMENT '部门名称',
  `oper_url` varchar(255) DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) DEFAULT '' COMMENT '主机地址',
  `oper_location` varchar(255) DEFAULT '' COMMENT '操作地点',
  `oper_param` varchar(2000) DEFAULT '' COMMENT '请求参数',
  `json_result` varchar(2000) DEFAULT '' COMMENT '返回参数',
  `status` int(1) DEFAULT '0' COMMENT '操作状态（0正常 1异常）',
  `error_msg` varchar(2000) DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime DEFAULT NULL COMMENT '操作时间',
  `cost_time` bigint(20) DEFAULT '0' COMMENT '消耗时间',
  PRIMARY KEY (`oper_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='操作日志记录';

-- 14. sys_logininfor
DROP TABLE IF EXISTS `sys_logininfor`;
CREATE TABLE `sys_logininfor` (
  `info_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '访问ID',
  `user_name` varchar(50) DEFAULT '' COMMENT '用户账号',
  `ipaddr` varchar(128) DEFAULT '' COMMENT '登录IP地址',
  `login_location` varchar(255) DEFAULT '' COMMENT '登录地点',
  `browser` varchar(50) DEFAULT '' COMMENT '浏览器类型',
  `os` varchar(50) DEFAULT '' COMMENT '操作系统',
  `status` char(1) DEFAULT '0' COMMENT '登录状态（0成功 1失败）',
  `msg` varchar(255) DEFAULT '' COMMENT '提示消息',
  `login_time` datetime DEFAULT NULL COMMENT '访问时间',
  PRIMARY KEY (`info_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='系统访问记录';

-- 15. sys_job (Quartz任务表)
DROP TABLE IF EXISTS `sys_job`;
CREATE TABLE `sys_job` (
  `job_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `job_name` varchar(64) NOT NULL DEFAULT '' COMMENT '任务名称',
  `job_group` varchar(64) NOT NULL DEFAULT '' COMMENT '任务组名',
  `invoke_target` varchar(500) NOT NULL COMMENT '调用目标字符串',
  `cron_expression` varchar(255) DEFAULT '' COMMENT 'cron执行表达式',
  `misfire_policy` varchar(20) DEFAULT '3' COMMENT '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
  `concurrent` char(1) DEFAULT '1' COMMENT '是否并发执行（0允许 1禁止）',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1暂停）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT '' COMMENT '备注信息',
  PRIMARY KEY (`job_id`,`job_name`,`job_group`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='定时任务调度表';

-- 16. sys_job_log
DROP TABLE IF EXISTS `sys_job_log`;
CREATE TABLE `sys_job_log` (
  `job_log_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务日志ID',
  `job_name` varchar(64) NOT NULL COMMENT '任务名称',
  `job_group` varchar(64) NOT NULL COMMENT '任务组名',
  `invoke_target` varchar(500) NOT NULL COMMENT '调用目标字符串',
  `job_message` varchar(500) DEFAULT NULL COMMENT '日志信息',
  `status` char(1) DEFAULT '0' COMMENT '执行状态（0正常 1失败）',
  `exception_info` varchar(2000) DEFAULT '' COMMENT '异常信息',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`job_log_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='定时任务调度日志表';

-- ============================================================
-- 第二部分: 商城业务表 (30张)
-- ============================================================

-- 1. 商家主表
DROP TABLE IF EXISTS `merchant`;
CREATE TABLE `merchant` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商家ID',
  `distributor_id` BIGINT DEFAULT NULL COMMENT '所属分销商ID',
  `name` VARCHAR(100) NOT NULL COMMENT '商家名称',
  `logo` VARCHAR(255) DEFAULT '' COMMENT '商家Logo',
  `contact` VARCHAR(50) DEFAULT '' COMMENT '联系人',
  `phone` VARCHAR(20) DEFAULT '' COMMENT '联系电话',
  `commission_rate` DECIMAL(5,2) DEFAULT 10.00 COMMENT '抽成比例(%)',
  `status` TINYINT DEFAULT 2 COMMENT '状态(0禁用 1正常 2待审核)',
  `balance` DECIMAL(12,2) DEFAULT 0.00 COMMENT '账户余额',
  `total_income` DECIMAL(12,2) DEFAULT 0.00 COMMENT '总收入',
  `address` VARCHAR(255) DEFAULT '' COMMENT '地址',
  `avatar` VARCHAR(255) DEFAULT '' COMMENT '商家头像/封面',
  `description` VARCHAR(500) DEFAULT '' COMMENT '商家简介',
  `business_hours` VARCHAR(100) DEFAULT '' COMMENT '营业时间',
  `product_count` INT DEFAULT 0 COMMENT '商品数量',
  `store_count` INT DEFAULT 0 COMMENT '门店数量',
  `c_mini_app_id` VARCHAR(64) DEFAULT NULL COMMENT 'C端小程序AppID',
  `c_mini_app_secret` VARCHAR(128) DEFAULT NULL COMMENT 'C端小程序Secret',
  `m_mini_app_id` VARCHAR(64) DEFAULT NULL COMMENT '商家端小程序AppID',
  `m_mini_app_secret` VARCHAR(128) DEFAULT NULL COMMENT '商家端小程序Secret',
  `wx_pay_mch_id` VARCHAR(64) DEFAULT NULL COMMENT '微信支付商户号',
  `wx_pay_api_key` VARCHAR(256) DEFAULT NULL COMMENT '微信支付API密钥',
  `receiver_type` VARCHAR(32) DEFAULT NULL COMMENT '收款方类型(OPENID/BANK)',
  `receiver_openid` VARCHAR(128) DEFAULT NULL COMMENT '微信收款openId',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_distributor_id` (`distributor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家主表';

-- 2. 商家门店表
DROP TABLE IF EXISTS `merchant_store`;
CREATE TABLE `merchant_store` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '门店ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `name` VARCHAR(100) NOT NULL COMMENT '门店名称',
  `contact` VARCHAR(50) DEFAULT '' COMMENT '联系人',
  `phone` VARCHAR(20) DEFAULT '' COMMENT '联系电话',
  `address` VARCHAR(255) DEFAULT '' COMMENT '门店地址',
  `longitude` DECIMAL(10,7) DEFAULT NULL COMMENT '经度',
  `latitude` DECIMAL(10,7) DEFAULT NULL COMMENT '纬度',
  `business_hours` VARCHAR(100) DEFAULT '' COMMENT '营业时间',
  `avatar` VARCHAR(255) DEFAULT '' COMMENT '门店封面图',
  `status` TINYINT DEFAULT 1 COMMENT '状态(0禁用 1正常)',
  `is_main` TINYINT DEFAULT 0 COMMENT '是否主门店(0否 1是)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家门店表';

-- 3. 商家登录账号表
DROP TABLE IF EXISTS `merchant_user`;
CREATE TABLE `merchant_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `username` VARCHAR(64) NOT NULL COMMENT '登录账号',
  `password` VARCHAR(128) NOT NULL COMMENT '登录密码',
  `real_name` VARCHAR(50) DEFAULT '' COMMENT '真实姓名',
  `phone` VARCHAR(20) DEFAULT '' COMMENT '手机号',
  `role` VARCHAR(20) DEFAULT 'member' COMMENT '角色(owner管理员/member成员)',
  `status` TINYINT DEFAULT 1 COMMENT '状态(0禁用 1正常)',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家登录账号表';

-- 4. 商品分类表
DROP TABLE IF EXISTS `product_category`;
CREATE TABLE `product_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `sort` INT DEFAULT 0 COMMENT '排序(越大越前)',
  `status` TINYINT DEFAULT 1 COMMENT '状态(0禁用 1正常)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 5. 团购活动表
DROP TABLE IF EXISTS `groupon_activity`;
CREATE TABLE `groupon_activity` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `name` VARCHAR(100) NOT NULL COMMENT '活动名称',
  `cover_image` VARCHAR(255) DEFAULT '' COMMENT '封面图',
  `description` VARCHAR(500) DEFAULT '' COMMENT '活动描述',
  `source_type` VARCHAR(20) DEFAULT 'ADMIN' COMMENT '来源(ADMIN后台/MERCHANT商家)',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `status` TINYINT DEFAULT 0 COMMENT '状态(0未开始 1进行中 2已结束 3已下架)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团购活动表';

-- 6. 商品表
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
  `groupon_id` BIGINT DEFAULT NULL COMMENT '关联团购活动ID',
  `name` VARCHAR(200) NOT NULL COMMENT '商品名称',
  `description` VARCHAR(500) DEFAULT '' COMMENT '商品描述',
  `cover_image` VARCHAR(255) DEFAULT '' COMMENT '封面图',
  `main_image` VARCHAR(255) DEFAULT NULL COMMENT '主图',
  `images` VARCHAR(2000) DEFAULT '' COMMENT '商品图组(JSON数组)',
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '团购价',
  `original_price` DECIMAL(10,2) DEFAULT 0.00 COMMENT '原价',
  `stock` INT DEFAULT 0 COMMENT '库存',
  `sales` INT DEFAULT 0 COMMENT '销量',
  `status` TINYINT DEFAULT 0 COMMENT '状态(0下架 1上架)',
  `sort` INT DEFAULT 0 COMMENT '排序(越大越前)',
  `valid_days` INT DEFAULT 30 COMMENT '购买后有效天数',
  `verify_notice` VARCHAR(255) DEFAULT NULL COMMENT '核销提示',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_groupon_id` (`groupon_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 7. 优惠券模板表
DROP TABLE IF EXISTS `coupon`;
CREATE TABLE `coupon` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
  `merchant_id` BIGINT DEFAULT NULL COMMENT '商家ID(NULL为平台券)',
  `name` VARCHAR(100) NOT NULL COMMENT '优惠券名称',
  `type` TINYINT NOT NULL COMMENT '类型(1满减 2折扣)',
  `discount_value` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠值',
  `min_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '使用门槛',
  `total_count` INT DEFAULT 0 COMMENT '发放总量',
  `remain_count` INT DEFAULT 0 COMMENT '剩余数量',
  `start_time` DATETIME DEFAULT NULL COMMENT '有效期开始',
  `end_time` DATETIME DEFAULT NULL COMMENT '有效期结束',
  `status` TINYINT DEFAULT 1 COMMENT '状态(0禁用 1正常)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券模板表';

-- 8. 用户优惠券表
DROP TABLE IF EXISTS `user_coupon`;
CREATE TABLE `user_coupon` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `coupon_id` BIGINT NOT NULL COMMENT '优惠券ID',
  `merchant_id` BIGINT DEFAULT NULL COMMENT '商家ID',
  `order_no` VARCHAR(32) DEFAULT '' COMMENT '关联订单号',
  `status` TINYINT DEFAULT 0 COMMENT '状态(0未使用 1已使用 2已过期)',
  `use_time` DATETIME DEFAULT NULL COMMENT '使用时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_coupon_id` (`coupon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

-- 9. 用户收藏表
DROP TABLE IF EXISTS `user_favorite`;
CREATE TABLE `user_favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `target_id` BIGINT NOT NULL COMMENT '目标ID(商家ID/商品ID)',
  `target_type` VARCHAR(20) NOT NULL COMMENT '目标类型(merchant/product)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';

-- 10. 用户地址表
DROP TABLE IF EXISTS `user_address`;
CREATE TABLE `user_address` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `name` VARCHAR(50) NOT NULL COMMENT '收货人',
  `phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
  `address` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `is_default` TINYINT DEFAULT 0 COMMENT '是否默认(0否 1是)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户地址表';

-- 11. C端用户表
DROP TABLE IF EXISTS `mall_user`;
CREATE TABLE `mall_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `open_id` VARCHAR(64) NOT NULL COMMENT '微信openId',
  `union_id` VARCHAR(64) DEFAULT NULL COMMENT '微信unionId',
  `nick_name` VARCHAR(100) DEFAULT '' COMMENT '昵称',
  `avatar_url` VARCHAR(255) DEFAULT '' COMMENT '头像',
  `phone` VARCHAR(20) DEFAULT '' COMMENT '手机号',
  `gender` TINYINT DEFAULT 0 COMMENT '性别(0未知 1男 2女)',
  `status` TINYINT DEFAULT 1 COMMENT '状态(0禁用 1正常)',
  `total_orders` INT DEFAULT 0 COMMENT '总订单数',
  `total_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '总消费金额',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_open_id` (`open_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='C端用户表';

-- 12. 分销商表
DROP TABLE IF EXISTS `distributor`;
CREATE TABLE `distributor` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分销商ID',
  `name` VARCHAR(100) NOT NULL COMMENT '分销商名称',
  `contact` VARCHAR(50) DEFAULT '' COMMENT '联系人',
  `phone` VARCHAR(20) DEFAULT '' COMMENT '联系电话',
  `username` VARCHAR(64) DEFAULT NULL COMMENT '登录账号',
  `password` VARCHAR(200) DEFAULT NULL COMMENT '登录密码(BCrypt)',
  `region_code` VARCHAR(20) DEFAULT NULL COMMENT '区域编码',
  `region_name` VARCHAR(50) DEFAULT NULL COMMENT '区域名称',
  `receiver_openid` VARCHAR(128) DEFAULT NULL COMMENT '微信收款openId',
  `receiver_type` VARCHAR(32) DEFAULT 'OPENID' COMMENT '收款方类型(OPENID/BANK)',
  `status` TINYINT DEFAULT 1 COMMENT '状态(0禁用 1正常)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分销商表';

-- 13. 订单主表
DROP TABLE IF EXISTS `mall_order`;
CREATE TABLE `mall_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `store_id` BIGINT DEFAULT NULL COMMENT '核销门店ID',
  `total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
  `pay_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '实付金额',
  `commission` DECIMAL(10,2) DEFAULT 0.00 COMMENT '平台佣金',
  `merchant_income` DECIMAL(10,2) DEFAULT 0.00 COMMENT '商家收入',
  `coupon_id` BIGINT DEFAULT NULL COMMENT '使用的优惠券ID',
  `coupon_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠券减免金额',
  `groupon_id` BIGINT DEFAULT NULL COMMENT '团购活动ID',
  `status` INT DEFAULT 0 COMMENT '状态(0待支付 1已支付 2已使用 3已完成 4已退款 5已取消)',
  `write_off_code` VARCHAR(32) DEFAULT NULL COMMENT '核销码',
  `write_off_status` INT DEFAULT 0 COMMENT '核销状态(0未核销 1已核销)',
  `write_off_time` DATETIME DEFAULT NULL COMMENT '核销时间',
  `write_off_user_id` BIGINT DEFAULT NULL COMMENT '核销操作人ID',
  `valid_days` INT DEFAULT NULL COMMENT '商品有效天数',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `use_time` DATETIME DEFAULT NULL COMMENT '使用时间',
  `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
  `refund_time` DATETIME DEFAULT NULL COMMENT '退款时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_status` (`status`),
  KEY `idx_write_off_code` (`write_off_code`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

-- 14. 订单商品明细表
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(32) DEFAULT '' COMMENT '订单编号',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `product_name` VARCHAR(200) DEFAULT '' COMMENT '商品名称',
  `product_image` VARCHAR(255) DEFAULT '' COMMENT '商品图片',
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '单价',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
  `subtotal` DECIMAL(10,2) DEFAULT 0.00 COMMENT '小计',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单商品明细表';

-- 15. 资金流水表
DROP TABLE IF EXISTS `transaction_record`;
CREATE TABLE `transaction_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '流水ID',
  `merchant_id` BIGINT DEFAULT NULL COMMENT '商家ID(NULL则为平台级)',
  `type` VARCHAR(20) NOT NULL COMMENT '类型(payment/income/withdraw/refund/commission)',
  `amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '金额',
  `balance` DECIMAL(12,2) DEFAULT 0.00 COMMENT '变动后余额',
  `order_no` VARCHAR(32) DEFAULT '' COMMENT '关联订单号',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_type` (`type`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资金流水表';

-- 16. 提现记录表
DROP TABLE IF EXISTS `withdraw_record`;
CREATE TABLE `withdraw_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '提现记录ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '提现金额',
  `bank_name` VARCHAR(100) DEFAULT '' COMMENT '银行名称',
  `bank_account` VARCHAR(50) DEFAULT '' COMMENT '银行卡号',
  `account_name` VARCHAR(50) DEFAULT '' COMMENT '开户名',
  `status` INT DEFAULT 0 COMMENT '状态(0待审核 1审核通过 2已打款 3拒绝)',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '拒绝原因/备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提现记录表';

-- 17. 平台收入表
DROP TABLE IF EXISTS `platform_income`;
CREATE TABLE `platform_income` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收入ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号',
  `order_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '订单金额',
  `commission_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT '抽成比例(%)',
  `commission` DECIMAL(10,2) DEFAULT 0.00 COMMENT '佣金金额',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台收入表';

-- 18. 商家账单表
DROP TABLE IF EXISTS `merchant_bill`;
CREATE TABLE `merchant_bill` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '账单ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `bill_period` VARCHAR(10) NOT NULL COMMENT '账单周期(如2026-05)',
  `total_orders` INT DEFAULT 0 COMMENT '总订单数',
  `total_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '总金额',
  `total_commission` DECIMAL(12,2) DEFAULT 0.00 COMMENT '总佣金',
  `net_income` DECIMAL(12,2) DEFAULT 0.00 COMMENT '净收入',
  `status` TINYINT DEFAULT 0 COMMENT '状态(0待结算 1已结算)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家账单表';

-- 19. 购物车表
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `product_name` VARCHAR(200) DEFAULT '' COMMENT '商品名称',
  `product_image` VARCHAR(255) DEFAULT '' COMMENT '商品图片',
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '单价',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 20. 首页轮播图表
DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '轮播ID',
  `merchant_id` BIGINT DEFAULT NULL COMMENT '商家ID(NULL为平台级)',
  `title` VARCHAR(100) DEFAULT '' COMMENT '标题',
  `image_url` VARCHAR(255) NOT NULL COMMENT '图片地址',
  `link_url` VARCHAR(255) DEFAULT '' COMMENT '跳转链接',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态(0禁用 1正常)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页轮播图表';

-- 21. 操作日志表
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `merchant_id` BIGINT DEFAULT NULL COMMENT '商家ID',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
  `operator_type` VARCHAR(20) DEFAULT '' COMMENT '操作人类型',
  `action` VARCHAR(100) NOT NULL COMMENT '操作动作',
  `target` VARCHAR(100) DEFAULT '' COMMENT '操作对象',
  `detail` VARCHAR(500) DEFAULT '' COMMENT '详情',
  `ip_address` VARCHAR(50) DEFAULT '' COMMENT 'IP地址',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_action` (`action`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 22. 微信用户信息表
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `user_id` VARCHAR(64) NOT NULL COMMENT '用户标识',
  `open_id` VARCHAR(64) DEFAULT NULL COMMENT '微信openId',
  `union_id` VARCHAR(64) DEFAULT NULL COMMENT '微信unionId',
  `user_name` VARCHAR(100) DEFAULT NULL COMMENT '用户昵称',
  `user_type` VARCHAR(10) DEFAULT NULL COMMENT '用户类型',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '头像Url',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_open_id` (`open_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信用户信息表';

-- 23. 商品图片表
DROP TABLE IF EXISTS `product_image`;
CREATE TABLE `product_image` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `image_type` VARCHAR(20) DEFAULT 'main' COMMENT '图片类型(main/detail/sku)',
  `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `sku_value` VARCHAR(100) DEFAULT NULL COMMENT 'SKU值',
  `status` TINYINT DEFAULT 1 COMMENT '状态(0删除 1正常)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品图片表';

-- 24. 核销记录表
DROP TABLE IF EXISTS `write_off_record`;
CREATE TABLE `write_off_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(32) DEFAULT NULL COMMENT '订单编号',
  `write_off_code` VARCHAR(32) DEFAULT NULL COMMENT '核销码',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `store_id` BIGINT DEFAULT NULL COMMENT '门店ID',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
  `write_off_type` TINYINT DEFAULT 1 COMMENT '核销类型(1扫码 2手动)',
  `write_off_time` DATETIME DEFAULT NULL COMMENT '核销时间',
  `product_name` VARCHAR(200) DEFAULT NULL COMMENT '商品名称',
  `product_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '商品金额',
  `status` TINYINT DEFAULT 1 COMMENT '状态(1有效 0作废)',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='核销记录表';

-- 25. 订单三方分账流水表
DROP TABLE IF EXISTS `order_profit_ledger`;
CREATE TABLE `order_profit_ledger` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `distributor_id` BIGINT DEFAULT NULL COMMENT '分销商ID',
  `pay_amount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '用户实付金额，单位元',
  `merchant_amount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '商家应得金额',
  `platform_amount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '平台抽成金额',
  `distributor_amount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '分销商佣金金额',
  `merchant_rate` DECIMAL(5,2) DEFAULT NULL COMMENT '商家比例',
  `platform_rate` DECIMAL(5,2) DEFAULT NULL COMMENT '平台比例',
  `distributor_rate` DECIMAL(5,2) DEFAULT NULL COMMENT '分销商比例',
  `status` VARCHAR(32) NOT NULL DEFAULT 'WAITING_SETTLEMENT' COMMENT 'WAITING_SETTLEMENT/SETTLED/REFUND_REVERSED',
  `finish_time` DATETIME DEFAULT NULL COMMENT '订单完成时间',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_distributor_id` (`distributor_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单三方分账流水表';

-- 26. 商家结算记录表
DROP TABLE IF EXISTS `merchant_settlement_record`;
CREATE TABLE `merchant_settlement_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '结算记录ID',
  `settlement_no` VARCHAR(64) NOT NULL COMMENT '结算单号',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `distributor_id` BIGINT DEFAULT NULL COMMENT '分销商ID',
  `store_id` BIGINT DEFAULT NULL COMMENT '门店ID',
  `order_no` VARCHAR(64) DEFAULT NULL COMMENT '关联订单号',
  `title` VARCHAR(255) DEFAULT NULL COMMENT '结算标题/商品名称',
  `order_amount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '订单金额',
  `merchant_amount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '商家结算金额',
  `platform_fee_amount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '平台佣金',
  `status` VARCHAR(32) NOT NULL DEFAULT 'WAITING_T1' COMMENT 'WAITING_T1/TRANSFERRING/ARRIVED/FAILED/CANCELLED/REFUND_PROCESSING/REVERSED',
  `apply_time` DATETIME DEFAULT NULL COMMENT '进入结算链路时间',
  `expected_transfer_time` DATETIME DEFAULT NULL COMMENT '预计打款时间',
  `transfer_time` DATETIME DEFAULT NULL COMMENT '发起打款时间',
  `arrive_time` DATETIME DEFAULT NULL COMMENT '到账时间',
  `fail_reason` VARCHAR(500) DEFAULT NULL COMMENT '失败原因',
  `wechat_batch_no` VARCHAR(128) DEFAULT NULL COMMENT '微信批次号',
  `wechat_detail_no` VARCHAR(128) DEFAULT NULL COMMENT '微信明细单号',
  `reverse_record_id` BIGINT DEFAULT NULL COMMENT '逆向/负向记录ID',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_settlement_no` (`settlement_no`),
  KEY `idx_merchant_status` (`merchant_id`, `status`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_expected_transfer_time` (`expected_transfer_time`),
  KEY `idx_distributor_id` (`distributor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家结算记录表';

-- 27. 分销商佣金结算记录表
DROP TABLE IF EXISTS `distributor_settlement_record`;
CREATE TABLE `distributor_settlement_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `settlement_no` VARCHAR(64) NOT NULL COMMENT '结算单号',
  `distributor_id` BIGINT NOT NULL COMMENT '分销商ID',
  `merchant_id` BIGINT DEFAULT NULL COMMENT '关联商家ID',
  `order_no` VARCHAR(64) DEFAULT NULL COMMENT '关联订单号',
  `amount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '佣金金额',
  `rate` DECIMAL(5,2) DEFAULT NULL COMMENT '佣金比例',
  `status` VARCHAR(32) NOT NULL DEFAULT 'WAITING_SETTLEMENT' COMMENT 'WAITING_SETTLEMENT/TRANSFERRING/ARRIVED/FAILED/CANCELLED/REVERSED',
  `settlement_period_start` DATE DEFAULT NULL COMMENT '结算周期开始',
  `settlement_period_end` DATE DEFAULT NULL COMMENT '结算周期结束',
  `expected_transfer_time` DATETIME DEFAULT NULL COMMENT '预计打款时间',
  `transfer_time` DATETIME DEFAULT NULL COMMENT '发起打款时间',
  `arrive_time` DATETIME DEFAULT NULL COMMENT '到账时间',
  `fail_reason` VARCHAR(500) DEFAULT NULL COMMENT '失败原因',
  `reverse_record_id` BIGINT DEFAULT NULL COMMENT '逆向记录ID',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_settlement_no` (`settlement_no`),
  KEY `idx_distributor_id` (`distributor_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_status` (`status`),
  KEY `idx_expected_transfer_time` (`expected_transfer_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分销商佣金结算记录表';

-- 28. 平台转账记录表
DROP TABLE IF EXISTS `platform_transfer_record`;
CREATE TABLE `platform_transfer_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `transfer_no` VARCHAR(64) NOT NULL COMMENT '转账单号',
  `settlement_no` VARCHAR(64) NOT NULL COMMENT '关联结算单号',
  `merchant_id` BIGINT DEFAULT NULL COMMENT '商家ID',
  `distributor_id` BIGINT DEFAULT NULL COMMENT '分销商ID',
  `target_type` VARCHAR(32) NOT NULL COMMENT '目标类型(merchant/distributor)',
  `target_id` BIGINT NOT NULL COMMENT '目标ID',
  `amount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '转账金额',
  `status` VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/TRANSFERRING/SUCCESS/FAILED',
  `wechat_batch_no` VARCHAR(128) DEFAULT NULL COMMENT '微信转账批次号',
  `wechat_detail_no` VARCHAR(128) DEFAULT NULL COMMENT '微信转账明细单号',
  `apply_time` DATETIME DEFAULT NULL COMMENT '申请转账时间',
  `transfer_time` DATETIME DEFAULT NULL COMMENT '发起转账时间',
  `arrive_time` DATETIME DEFAULT NULL COMMENT '到账时间',
  `notify_time` DATETIME DEFAULT NULL COMMENT '微信回调时间',
  `notify_result` TEXT DEFAULT NULL COMMENT '微信回调原始结果',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
  `fail_reason` VARCHAR(500) DEFAULT NULL COMMENT '失败原因',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transfer_no` (`transfer_no`),
  KEY `idx_settlement_no` (`settlement_no`),
  KEY `idx_target` (`target_type`, `target_id`),
  KEY `idx_status` (`status`),
  KEY `idx_wechat_batch_no` (`wechat_batch_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台微信转账记录表';

-- 29. 支付记录表
DROP TABLE IF EXISTS `payment_record`;
CREATE TABLE `payment_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '支付记录ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
  `transaction_id` VARCHAR(64) DEFAULT NULL COMMENT '微信交易号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '支付金额',
  `pay_type` VARCHAR(20) DEFAULT 'wechat' COMMENT '支付方式',
  `pay_channel` VARCHAR(20) DEFAULT 'WECHAT' COMMENT '支付渠道',
  `pay_mch_id` VARCHAR(64) DEFAULT NULL COMMENT '微信商户号',
  `pay_status` VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/SUCCESS/FAILED/REFUND',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `notify_raw` TEXT DEFAULT NULL COMMENT '微信回调原始报文',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_transaction_id` (`transaction_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- 30. 退款记录表
DROP TABLE IF EXISTS `refund_record`;
CREATE TABLE `refund_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '退款记录ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
  `refund_no` VARCHAR(64) DEFAULT NULL COMMENT '退款单号',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `refund_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '退款金额',
  `refund_reason` VARCHAR(255) DEFAULT '' COMMENT '退款原因',
  `refund_type` VARCHAR(20) DEFAULT 'USER' COMMENT '退款类型(USER用户申请/ADMIN平台操作)',
  `status` INT DEFAULT 0 COMMENT '状态(0待审核 1审核通过 2退款中 3已打款 4退款完成 5退款异常)',
  `apply_time` DATETIME DEFAULT NULL COMMENT '申请时间',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `refund_time` DATETIME DEFAULT NULL COMMENT '退款完成时间',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_refund_type` (`refund_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款记录表';

-- 31. 分销商角色业务范围表
DROP TABLE IF EXISTS `sys_user_biz_scope`;
CREATE TABLE `sys_user_biz_scope` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `account_type` VARCHAR(32) DEFAULT 'PLATFORM' COMMENT '账号类型',
  `distributor_id` BIGINT DEFAULT NULL COMMENT '分销商ID',
  `merchant_id` BIGINT DEFAULT NULL COMMENT '商家ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户业务范围表';

-- ============================================================
-- 第三部分: 初始化数据
-- ============================================================

-- 插入默认管理员（密码: admin123）
DELETE FROM `sys_user` WHERE `user_id` = 1;
INSERT INTO `sys_user` (`user_id`, `user_name`, `nick_name`, `password`, `status`, `account_type`, `create_time`) VALUES
(1, 'admin', '超级管理员', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', 'SUPER_ADMIN', NOW());

-- 插入测试账号
DELETE FROM `sys_user` WHERE `user_name` IN ('dist_east', 'dist_south', 'dist_north');
INSERT INTO `sys_user` (`user_name`, `nick_name`, `password`, `status`, `account_type`, `distributor_id`, `create_time`) VALUES
('dist_east', '华东分销商', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', 'DISTRIBUTOR_ADMIN', 2, NOW()),
('dist_south', '华南分销商', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', 'DISTRIBUTOR_ADMIN', 3, NOW()),
('dist_north', '华北分销商', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', 'DISTRIBUTOR_ADMIN', 4, NOW());

-- 插入测试分销商
DELETE FROM `distributor` WHERE `id` IN (2,3,4);
INSERT INTO `distributor` (`id`, `name`, `contact`, `phone`, `username`, `password`, `region_code`, `region_name`, `status`, `create_time`) VALUES
(2, '华东分销商', '张经理', '13800000002', 'dist_east', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'SH', '上海市', 1, NOW()),
(3, '华南分销商', '李经理', '13800000003', 'dist_south', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'GZ', '广州市', 1, NOW()),
(4, '华北分销商', '王经理', '13800000004', 'dist_north', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'BJ', '北京市', 1, NOW());

-- 插入测试商品分类（用于product_category）
DELETE FROM `product_category` WHERE `id` <= 10;
INSERT INTO `product_category` (`id`, `merchant_id`, `name`, `sort`, `status`, `create_time`) VALUES
(1, 1, '招牌推荐', 100, 1, NOW()),
(2, 1, '人气套餐', 90, 1, NOW()),
(3, 1, '优惠活动', 80, 1, NOW());

-- 为商户创建初始员工账号（密码: 123456）
DELETE FROM `merchant_user` WHERE `id` <= 10;
INSERT INTO `merchant_user` (`id`, `merchant_id`, `username`, `password`, `real_name`, `role`, `status`, `create_time`) VALUES
(1, 1, 'admin1', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '店长', 'owner', 1, NOW());

-- 系统配置
DELETE FROM `sys_config` WHERE `config_key` IN ('sys.user.initPassword', 'sys.register.enabled');
INSERT INTO `sys_config` (`config_name`, `config_key`, `config_value`, `config_type`) VALUES
('用户初始密码', 'sys.user.initPassword', '123456', 'Y'),
('注册功能', 'sys.register.enabled', 'false', 'Y');
