-- ============================================
-- 零点科技多商家SaaS团购商城 — 數據庫一鍵補全腳本
-- 數據庫: ruoyi-cs
-- 字符集: utf8mb4
-- 執行順序說明（按階段依序執行，不可亂序）
-- ============================================
-- 執行方式：
--   方式一：mysql -u root -p < sql/master_complete.sql
--   方式二：在 MySQL 客戶端中執行 source sql/master_complete.sql;
-- ============================================

-- ============================================
-- 階段 0：創建數據庫（如尚未創建）
-- ============================================
CREATE DATABASE IF NOT EXISTS `ruoyi-cs` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `ruoyi-cs`;

-- ================================================================
-- 階段 1：若依系統基礎表（18張）+ 初始數據
-- 來源：sql/ry_20250417.sql（精簡版，只保留核心表）
-- ================================================================

-- 1.1 部門表
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
  `dept_id`           bigint(20)      NOT NULL AUTO_INCREMENT    COMMENT '部门id',
  `parent_id`         bigint(20)      DEFAULT 0                  COMMENT '父部门id',
  `ancestors`         varchar(50)     DEFAULT ''                 COMMENT '祖级列表',
  `dept_name`         varchar(30)     DEFAULT ''                 COMMENT '部门名称',
  `order_num`         int(4)          DEFAULT 0                  COMMENT '显示顺序',
  `leader`            varchar(20)     DEFAULT NULL               COMMENT '负责人',
  `phone`             varchar(11)     DEFAULT NULL               COMMENT '联系电话',
  `email`             varchar(50)     DEFAULT NULL               COMMENT '邮箱',
  `status`            char(1)         DEFAULT '0'                COMMENT '部门状态（0正常 1停用）',
  `del_flag`          char(1)         DEFAULT '0'                COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by`         varchar(64)     DEFAULT ''                 COMMENT '创建者',
  `create_time`       datetime                                   COMMENT '创建时间',
  `update_by`         varchar(64)     DEFAULT ''                 COMMENT '更新者',
  `update_time`       datetime                                   COMMENT '更新时间',
  PRIMARY KEY (`dept_id`)
) ENGINE=InnoDB AUTO_INCREMENT=200 DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 1.2 用戶表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `user_id`           bigint(20)      NOT NULL AUTO_INCREMENT    COMMENT '用户ID',
  `dept_id`           bigint(20)      DEFAULT NULL               COMMENT '部门ID',
  `user_name`         varchar(30)     NOT NULL                   COMMENT '用户账号',
  `nick_name`         varchar(30)     NOT NULL                   COMMENT '用户昵称',
  `user_type`         varchar(2)      DEFAULT '00'               COMMENT '用户类型（00系统用户）',
  `email`             varchar(50)     DEFAULT ''                 COMMENT '用户邮箱',
  `phonenumber`       varchar(11)     DEFAULT ''                 COMMENT '手机号码',
  `sex`               char(1)         DEFAULT '0'                COMMENT '用户性别（0男 1女 2未知）',
  `avatar`            varchar(100)    DEFAULT ''                 COMMENT '头像地址',
  `password`          varchar(100)    DEFAULT ''                 COMMENT '密码',
  `status`            char(1)         DEFAULT '0'                COMMENT '账号状态（0正常 1停用）',
  `del_flag`          char(1)         DEFAULT '0'                COMMENT '删除标志（0代表存在 2代表删除）',
  `login_ip`          varchar(128)    DEFAULT ''                 COMMENT '最后登录IP',
  `login_date`        datetime                                   COMMENT '最后登录时间',
  `create_by`         varchar(64)     DEFAULT ''                 COMMENT '创建者',
  `create_time`       datetime                                   COMMENT '创建时间',
  `update_by`         varchar(64)     DEFAULT ''                 COMMENT '更新者',
  `update_time`       datetime                                   COMMENT '更新时间',
  `remark`            varchar(500)    DEFAULT NULL               COMMENT '备注',
  -- 以下為商城擴展字段（由 hotfix SQL 補充）
  `account_type`      varchar(20)     DEFAULT 'PLATFORM'         COMMENT '账号类型: PLATFORM/DISTRIBUTOR/MERCHANT',
  `distributor_id`    bigint(20)      DEFAULT NULL               COMMENT '绑定分销商ID',
  `merchant_id`       bigint(20)      DEFAULT NULL               COMMENT '绑定商家ID',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- 1.3 崗位表
DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post` (
  `post_id`       bigint(20)      NOT NULL AUTO_INCREMENT    COMMENT '岗位ID',
  `post_code`     varchar(64)     NOT NULL                   COMMENT '岗位编码',
  `post_name`     varchar(50)     NOT NULL                   COMMENT '岗位名称',
  `post_sort`     int(4)          NOT NULL                   COMMENT '显示顺序',
  `status`        char(1)         NOT NULL                   COMMENT '状态（0正常 1停用）',
  `create_by`     varchar(64)     DEFAULT ''                 COMMENT '创建者',
  `create_time`   datetime                                   COMMENT '创建时间',
  `update_by`     varchar(64)     DEFAULT ''                 COMMENT '更新者',
  `update_time`   datetime                                   COMMENT '更新时间',
  `remark`        varchar(500)    DEFAULT NULL               COMMENT '备注',
  PRIMARY KEY (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位信息表';

-- 1.4 角色表
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `role_id`              bigint(20)      NOT NULL AUTO_INCREMENT    COMMENT '角色ID',
  `role_name`            varchar(30)     NOT NULL                   COMMENT '角色名称',
  `role_key`             varchar(100)    NOT NULL                   COMMENT '角色权限字符串',
  `role_sort`            int(4)          NOT NULL                   COMMENT '显示顺序',
  `data_scope`           char(1)         DEFAULT '1'                COMMENT '数据范围（1：全部 2：自定 3：本部门 4：本部门及以下 5：仅本人）',
  `menu_check_strictly`  tinyint(1)      DEFAULT 1                  COMMENT '菜单树选择项是否关联显示',
  `dept_check_strictly`  tinyint(1)      DEFAULT 1                  COMMENT '部门树选择项是否关联显示',
  `status`               char(1)         NOT NULL                   COMMENT '角色状态（0正常 1停用）',
  `del_flag`             char(1)         DEFAULT '0'                COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by`            varchar(64)     DEFAULT ''                 COMMENT '创建者',
  `create_time`          datetime                                   COMMENT '创建时间',
  `update_by`            varchar(64)     DEFAULT ''                 COMMENT '更新者',
  `update_time`          datetime                                   COMMENT '更新时间',
  `remark`               varchar(500)    DEFAULT NULL               COMMENT '备注',
  -- 商城擴展字段
  `role_scope`           varchar(20)     DEFAULT 'PLATFORM'         COMMENT '角色归属: PLATFORM/DISTRIBUTOR/MERCHANT',
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='角色信息表';

-- 1.5 菜單權限表
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `menu_id`           bigint(20)      NOT NULL AUTO_INCREMENT    COMMENT '菜单ID',
  `menu_name`         varchar(50)     NOT NULL                   COMMENT '菜单名称',
  `parent_id`         bigint(20)      DEFAULT 0                  COMMENT '父菜单ID',
  `order_num`         int(4)          DEFAULT 0                  COMMENT '显示顺序',
  `path`              varchar(200)    DEFAULT ''                 COMMENT '路由地址',
  `component`         varchar(255)    DEFAULT NULL               COMMENT '组件路径',
  `query`             varchar(255)    DEFAULT NULL               COMMENT '路由参数',
  `route_name`        varchar(50)     DEFAULT ''                 COMMENT '路由名称',
  `is_frame`          int(1)          DEFAULT 1                  COMMENT '是否为外链（0是 1否）',
  `is_cache`          int(1)          DEFAULT 0                  COMMENT '是否缓存（0缓存 1不缓存）',
  `menu_type`         char(1)         DEFAULT ''                 COMMENT '菜单类型（M目录 C菜单 F按钮）',
  `visible`           char(1)         DEFAULT 0                  COMMENT '菜单状态（0显示 1隐藏）',
  `status`            char(1)         DEFAULT 0                  COMMENT '菜单状态（0正常 1停用）',
  `perms`             varchar(100)    DEFAULT NULL               COMMENT '权限标识',
  `icon`              varchar(100)    DEFAULT '#'                COMMENT '菜单图标',
  `create_by`         varchar(64)     DEFAULT ''                 COMMENT '创建者',
  `create_time`       datetime                                   COMMENT '创建时间',
  `update_by`         varchar(64)     DEFAULT ''                 COMMENT '更新者',
  `update_time`       datetime                                   COMMENT '更新时间',
  `remark`            varchar(500)    DEFAULT ''                 COMMENT '备注',
  PRIMARY KEY (`menu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2000 DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- 1.6 用戶-角色關聯
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `user_id`   bigint(20) NOT NULL COMMENT '用户ID',
  `role_id`   bigint(20) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户和角色关联表';

-- 1.7 用戶-崗位關聯
DROP TABLE IF EXISTS `sys_user_post`;
CREATE TABLE `sys_user_post` (
  `user_id`   bigint(20) NOT NULL COMMENT '用户ID',
  `post_id`   bigint(20) NOT NULL COMMENT '岗位ID',
  PRIMARY KEY (`user_id`, `post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户与岗位关联表';

-- 1.8 角色-菜單關聯
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `role_id`   bigint(20) NOT NULL COMMENT '角色ID',
  `menu_id`   bigint(20) NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色和菜单关联表';

-- 1.9 角色-部門關聯
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept` (
  `role_id`   bigint(20) NOT NULL COMMENT '角色ID',
  `dept_id`   bigint(20) NOT NULL COMMENT '部门ID',
  PRIMARY KEY (`role_id`, `dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色和部门关联表';

-- 1.10 字典類型表
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type` (
  `dict_id`     bigint(20)      NOT NULL AUTO_INCREMENT    COMMENT '字典主键',
  `dict_name`   varchar(100)    DEFAULT ''                 COMMENT '字典名称',
  `dict_type`   varchar(100)    DEFAULT ''                 COMMENT '字典类型',
  `status`      char(1)         DEFAULT '0'                COMMENT '状态（0正常 1停用）',
  `create_by`   varchar(64)     DEFAULT ''                 COMMENT '创建者',
  `create_time` datetime                                   COMMENT '创建时间',
  `update_by`   varchar(64)     DEFAULT ''                 COMMENT '更新者',
  `update_time` datetime                                   COMMENT '更新时间',
  `remark`      varchar(500)    DEFAULT NULL               COMMENT '备注',
  PRIMARY KEY (`dict_id`),
  UNIQUE KEY `dict_type` (`dict_type`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='字典类型表';

-- 1.11 字典數據表
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
  `dict_code`   bigint(20)      NOT NULL AUTO_INCREMENT    COMMENT '字典编码',
  `dict_sort`   int(4)          DEFAULT 0                  COMMENT '字典排序',
  `dict_label`  varchar(100)    DEFAULT ''                 COMMENT '字典标签',
  `dict_value`  varchar(100)    DEFAULT ''                 COMMENT '字典键值',
  `dict_type`   varchar(100)    DEFAULT ''                 COMMENT '字典类型',
  `css_class`   varchar(100)    DEFAULT NULL               COMMENT '样式属性（其他样式扩展）',
  `list_class`  varchar(100)    DEFAULT NULL               COMMENT '表格回显样式',
  `is_default`  char(1)         DEFAULT 'N'                COMMENT '是否默认（Y是 N否）',
  `status`      char(1)         DEFAULT '0'                COMMENT '状态（0正常 1停用）',
  `create_by`   varchar(64)     DEFAULT ''                 COMMENT '创建者',
  `create_time` datetime                                   COMMENT '创建时间',
  `update_by`   varchar(64)     DEFAULT ''                 COMMENT '更新者',
  `update_time` datetime                                   COMMENT '更新时间',
  `remark`      varchar(500)    DEFAULT NULL               COMMENT '备注',
  PRIMARY KEY (`dict_code`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';

-- 1.12 參數配置表
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `config_id`     bigint(20)      NOT NULL AUTO_INCREMENT    COMMENT '参数主键',
  `config_name`   varchar(100)    DEFAULT ''                 COMMENT '参数名称',
  `config_key`    varchar(100)    DEFAULT ''                 COMMENT '参数键名',
  `config_value`  varchar(500)    DEFAULT ''                 COMMENT '参数键值',
  `config_type`   char(1)         DEFAULT 'N'                COMMENT '系统内置（Y是 N否）',
  `create_by`     varchar(64)     DEFAULT ''                 COMMENT '创建者',
  `create_time`   datetime                                   COMMENT '创建时间',
  `update_by`     varchar(64)     DEFAULT ''                 COMMENT '更新者',
  `update_time`   datetime                                   COMMENT '更新时间',
  `remark`        varchar(500)    DEFAULT NULL               COMMENT '备注',
  PRIMARY KEY (`config_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='参数配置表';

-- 1.13 通知公告表
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice` (
  `notice_id`       bigint(20)      NOT NULL AUTO_INCREMENT    COMMENT '公告ID',
  `notice_title`    varchar(50)     NOT NULL                   COMMENT '公告标题',
  `notice_type`     char(1)         NOT NULL                   COMMENT '公告类型（1通知 2公告）',
  `notice_content`  longblob        DEFAULT NULL               COMMENT '公告内容',
  `status`          char(1)         DEFAULT '0'                COMMENT '公告状态（0正常 1关闭）',
  `create_by`       varchar(64)     DEFAULT ''                 COMMENT '创建者',
  `create_time`     datetime                                   COMMENT '创建时间',
  `update_by`       varchar(64)     DEFAULT ''                 COMMENT '更新者',
  `update_time`     datetime                                   COMMENT '更新时间',
  `remark`          varchar(255)    DEFAULT NULL               COMMENT '备注',
  PRIMARY KEY (`notice_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COMMENT='通知公告表';

-- 1.14 登錄日誌表
DROP TABLE IF EXISTS `sys_logininfor`;
CREATE TABLE `sys_logininfor` (
  `info_id`         bigint(20)      NOT NULL AUTO_INCREMENT    COMMENT '访问ID',
  `user_name`       varchar(50)     DEFAULT ''                 COMMENT '用户账号',
  `ipaddr`          varchar(128)    DEFAULT ''                 COMMENT '登录IP地址',
  `login_location`  varchar(255)    DEFAULT ''                 COMMENT '登录地点',
  `browser`         varchar(50)     DEFAULT ''                 COMMENT '浏览器类型',
  `os`              varchar(50)     DEFAULT ''                 COMMENT '操作系统',
  `status`          char(1)         DEFAULT '0'                COMMENT '登录状态（0成功 1失败）',
  `msg`             varchar(255)    DEFAULT ''                 COMMENT '提示消息',
  `login_time`      datetime                                   COMMENT '访问时间',
  PRIMARY KEY (`info_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='系统访问记录';

-- 1.15 操作日誌表
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log` (
  `oper_id`         bigint(20)      NOT NULL AUTO_INCREMENT    COMMENT '日志主键',
  `title`           varchar(50)     DEFAULT ''                 COMMENT '模块标题',
  `business_type`   int(2)          DEFAULT 0                  COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `method`          varchar(100)    DEFAULT ''                 COMMENT '方法名称',
  `request_method`  varchar(10)     DEFAULT ''                 COMMENT '请求方式',
  `operator_type`   int(1)          DEFAULT 0                  COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
  `oper_name`       varchar(50)     DEFAULT ''                 COMMENT '操作人员',
  `dept_name`       varchar(50)     DEFAULT ''                 COMMENT '部门名称',
  `oper_url`        varchar(255)    DEFAULT ''                 COMMENT '请求URL',
  `oper_ip`         varchar(128)    DEFAULT ''                 COMMENT '主机地址',
  `oper_location`   varchar(255)    DEFAULT ''                 COMMENT '操作地点',
  `oper_param`      varchar(2000)   DEFAULT ''                 COMMENT '请求参数',
  `json_result`     varchar(2000)   DEFAULT ''                 COMMENT '返回参数',
  `status`          int(1)          DEFAULT 0                  COMMENT '操作状态（0正常 1异常）',
  `error_msg`       varchar(2000)   DEFAULT ''                 COMMENT '错误消息',
  `oper_time`       datetime                                   COMMENT '操作时间',
  `cost_time`       bigint(20)      DEFAULT 0                  COMMENT '消耗时间',
  PRIMARY KEY (`oper_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='操作日志记录';

-- 1.16 定時任務表（業務表，非 quartz 內部表）
DROP TABLE IF EXISTS `sys_job`;
CREATE TABLE `sys_job` (
  `job_id`            bigint(20)      NOT NULL AUTO_INCREMENT    COMMENT '任务ID',
  `job_name`          varchar(64)     NOT NULL DEFAULT ''        COMMENT '任务名称',
  `job_group`         varchar(64)     NOT NULL DEFAULT ''        COMMENT '任务组名',
  `invoke_target`     varchar(500)    NOT NULL                   COMMENT '调用目标字符串',
  `cron_expression`   varchar(255)    DEFAULT ''                 COMMENT 'cron执行表达式',
  `misfire_policy`    varchar(20)     DEFAULT '3'                COMMENT '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
  `concurrent`        char(1)         DEFAULT '1'                COMMENT '是否并发执行（0允许 1禁止）',
  `status`            char(1)         DEFAULT '0'                COMMENT '状态（0正常 1暂停）',
  `create_by`         varchar(64)     DEFAULT ''                 COMMENT '创建者',
  `create_time`       datetime                                   COMMENT '创建时间',
  `update_by`         varchar(64)     DEFAULT ''                 COMMENT '更新者',
  `update_time`       datetime                                   COMMENT '更新时间',
  `remark`            varchar(500)    DEFAULT ''                 COMMENT '备注信息',
  PRIMARY KEY (`job_id`, `job_name`, `job_group`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COMMENT='定时任务调度表';

-- 1.17 定時任務日誌
DROP TABLE IF EXISTS `sys_job_log`;
CREATE TABLE `sys_job_log` (
  `job_log_id`      bigint(20)      NOT NULL AUTO_INCREMENT    COMMENT '任务日志ID',
  `job_name`        varchar(64)     NOT NULL                   COMMENT '任务名称',
  `job_group`       varchar(64)     NOT NULL                   COMMENT '任务组名',
  `invoke_target`   varchar(500)    NOT NULL                   COMMENT '调用目标字符串',
  `job_message`     varchar(500)                              COMMENT '日志信息',
  `status`          char(1)         DEFAULT '0'                COMMENT '执行状态（0正常 1失败）',
  `exception_info`  varchar(2000)   DEFAULT ''                 COMMENT '异常信息',
  `create_time`     datetime                                   COMMENT '创建时间',
  PRIMARY KEY (`job_log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务调度日志表';

-- ================================================================
-- 階段 1 初始數據
-- ================================================================
INSERT INTO `sys_dept` VALUES
(100,  0,   '0',          '若依科技',   0, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', NOW(), '', NULL),
(101,  100, '0,100',      '深圳总公司', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', NOW(), '', NULL),
(102,  100, '0,100',      '长沙分公司', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', NOW(), '', NULL),
(103,  101, '0,100,101',  '研发部门',   1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', NOW(), '', NULL),
(104,  101, '0,100,101',  '市场部门',   2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', NOW(), '', NULL),
(105,  101, '0,100,101',  '测试部门',   3, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', NOW(), '', NULL),
(106,  101, '0,100,101',  '财务部门',   4, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', NOW(), '', NULL),
(107,  101, '0,100,101',  '运维部门',   5, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', NOW(), '', NULL),
(108,  102, '0,100,102',  '市场部门',   1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', NOW(), '', NULL),
(109,  102, '0,100,102',  '财务部门',   2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', NOW(), '', NULL);

-- 密碼 admin123（BCrypt）
INSERT INTO `sys_user` VALUES
(1, 103, 'admin', '若依', '00', 'ry@163.com', '15888888888', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', NOW(), 'admin', NOW(), '', NULL, '管理员', 'PLATFORM', NULL, NULL),
(2, 105, 'ry',    '若依', '00', 'ry@qq.com',  '15666666666', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', NOW(), 'admin', NOW(), '', NULL, '测试员', 'PLATFORM', NULL, NULL);

INSERT INTO `sys_post` VALUES
(1, 'ceo',  '董事长',    1, '0', 'admin', NOW(), '', NULL, ''),
(2, 'se',   '项目经理',  2, '0', 'admin', NOW(), '', NULL, ''),
(3, 'hr',   '人力资源',  3, '0', 'admin', NOW(), '', NULL, ''),
(4, 'user', '普通员工',  4, '0', 'admin', NOW(), '', NULL, '');

INSERT INTO `sys_role` VALUES
(1, '超级管理员', 'admin',  1, 1, 1, 1, '0', '0', 'admin', NOW(), '', NULL, '超级管理员', 'PLATFORM'),
(2, '普通角色',   'common', 2, 2, 1, 1, '0', '0', 'admin', NOW(), '', NULL, '普通角色', 'PLATFORM');

-- 菜單（根據商城功能擴展）
INSERT INTO `sys_menu` VALUES
-- 一級菜單
(1, '系统管理', '0', 1, 'system',    NULL,      '', '', 1, 0, 'M', '0', '0', '', 'system',   'admin', NOW(), '', NULL, '系统管理目录'),
(2, '系统监控', '0', 2, 'monitor',   NULL,      '', '', 1, 0, 'M', '0', '0', '', 'monitor',  'admin', NOW(), '', NULL, '系统监控目录'),
(3, '系统工具', '0', 3, 'tool',      NULL,      '', '', 1, 0, 'M', '0', '0', '', 'tool',     'admin', NOW(), '', NULL, '系统工具目录'),
(4, '商城管理', '0', 4, 'mall',      NULL,      '', '', 1, 0, 'M', '0', '0', '', 'shopping', 'admin', NOW(), '', NULL, '商城管理目录'),
-- 二級菜單（系統）
(100, '用户管理', 1, 1, 'user',     'system/user/index',   '', '', 1, 0, 'C', '0', '0', 'system:user:list',     'user',      'admin', NOW(), '', NULL, '用户管理菜单'),
(101, '角色管理', 1, 2, 'role',     'system/role/index',   '', '', 1, 0, 'C', '0', '0', 'system:role:list',     'peoples',   'admin', NOW(), '', NULL, '角色管理菜单'),
(102, '菜单管理', 1, 3, 'menu',     'system/menu/index',   '', '', 1, 0, 'C', '0', '0', 'system:menu:list',     'tree-table','admin', NOW(), '', NULL, '菜单管理菜单'),
(103, '部门管理', 1, 4, 'dept',     'system/dept/index',   '', '', 1, 0, 'C', '0', '0', 'system:dept:list',     'tree',      'admin', NOW(), '', NULL, '部门管理菜单'),
(104, '岗位管理', 1, 5, 'post',     'system/post/index',   '', '', 1, 0, 'C', '0', '0', 'system:post:list',     'post',      'admin', NOW(), '', NULL, '岗位管理菜单'),
(105, '字典管理', 1, 6, 'dict',     'system/dict/index',   '', '', 1, 0, 'C', '0', '0', 'system:dict:list',     'dict',      'admin', NOW(), '', NULL, '字典管理菜单'),
(106, '参数设置', 1, 7, 'config',   'system/config/index', '', '', 1, 0, 'C', '0', '0', 'system:config:list',   'edit',      'admin', NOW(), '', NULL, '参数设置菜单'),
(107, '通知公告', 1, 8, 'notice',   'system/notice/index', '', '', 1, 0, 'C', '0', '0', 'system:notice:list',   'message',   'admin', NOW(), '', NULL, '通知公告菜单'),
(108, '日志管理', 1, 9, 'log',      '',                    '', '', 1, 0, 'M', '0', '0', '',                     'log',       'admin', NOW(), '', NULL, '日志管理菜单'),
-- 二級菜單（監控）
(109, '在线用户', 2, 1, 'online',   'monitor/online/index',     '', '', 1, 0, 'C', '0', '0', 'monitor:online:list',  'online',    'admin', NOW(), '', NULL, ''),
(110, '定时任务', 2, 2, 'job',      'monitor/job/index',        '', '', 1, 0, 'C', '0', '0', 'monitor:job:list',     'job',       'admin', NOW(), '', NULL, ''),
-- 二級菜單（商城）
(200, '商家管理', 4, 1, 'merchant',      'mall/merchant/index',      '', '', 1, 0, 'C', '0', '0', 'mall:merchant:list',     'merchant',  'admin', NOW(), '', NULL, ''),
(201, '商品管理', 4, 2, 'product',       'mall/product/index',       '', '', 1, 0, 'C', '0', '0', 'mall:product:list',      'product',   'admin', NOW(), '', NULL, ''),
(202, '订单管理', 4, 3, 'order',         'mall/order/index',         '', '', 1, 0, 'C', '0', '0', 'mall:order:list',        'order',     'admin', NOW(), '', NULL, ''),
(203, '用户管理', 4, 4, 'mall-user',     'mall/user/index',          '', '', 1, 0, 'C', '0', '0', 'mall:user:list',         'user',      'admin', NOW(), '', NULL, ''),
(204, '财务管理', 4, 5, 'finance',       'mall/finance/index',       '', '', 1, 0, 'C', '0', '0', 'mall:finance:list',      'finance',   'admin', NOW(), '', NULL, ''),
(205, '优惠券管理', 4, 6, 'coupon',      'mall/coupon/index',        '', '', 1, 0, 'C', '0', '0', 'mall:coupon:list',       'coupon',    'admin', NOW(), '', NULL, ''),
(206, '轮播图管理', 4, 7, 'banner',      'mall/banner/index',        '', '', 1, 0, 'C', '0', '0', 'mall:banner:list',       'banner',    'admin', NOW(), '', NULL, ''),
(207, '分销商管理', 4, 8, 'distributor', 'mall/distributor/index',   '', '', 1, 0, 'C', '0', '0', 'mall:distributor:list',  'distributor','admin', NOW(), '', NULL, ''),
(208, '售后管理', 4, 9, 'after-sale',   'mall/after-sale/index',    '', '', 1, 0, 'C', '0', '0', 'mall:after-sale:list',   'refund',    'admin', NOW(), '', NULL, ''),
(209, '工作台', 4, 0, 'dashboard',     'mall/dashboard/index',      '', '', 1, 0, 'C', '0', '0', 'mall:dashboard:list',    'dashboard', 'admin', NOW(), '', NULL, '');

-- 角色-菜單（超管全部，common 部分）
INSERT INTO `sys_role_menu` SELECT 1, menu_id FROM sys_menu;
INSERT INTO `sys_role_menu` VALUES (2, 100), (2, 101), (2, 102), (2, 103), (2, 200), (2, 201), (2, 202), (2, 203);

-- 用戶-角色
INSERT INTO `sys_user_role` VALUES (1, 1), (2, 2);

-- ================================================================
-- 階段 2：Quartz 定時任務表（11張）
-- ================================================================
DROP TABLE IF EXISTS QRTZ_FIRED_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_PAUSED_TRIGGER_GRPS;
DROP TABLE IF EXISTS QRTZ_SCHEDULER_STATE;
DROP TABLE IF EXISTS QRTZ_LOCKS;
DROP TABLE IF EXISTS QRTZ_SIMPLE_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_SIMPROP_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_CRON_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_BLOB_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_JOB_DETAILS;
DROP TABLE IF EXISTS QRTZ_CALENDARS;

CREATE TABLE QRTZ_JOB_DETAILS (
  sched_name        VARCHAR(120) NOT NULL COMMENT '调度名称',
  job_name          VARCHAR(200) NOT NULL COMMENT '任务名称',
  job_group         VARCHAR(200) NOT NULL COMMENT '任务组名',
  description       VARCHAR(250) NULL COMMENT '相关介绍',
  job_class_name    VARCHAR(250) NOT NULL COMMENT '执行任务类名称',
  is_durable        VARCHAR(1)   NOT NULL COMMENT '是否持久化',
  is_nonconcurrent  VARCHAR(1)   NOT NULL COMMENT '是否并发',
  is_update_data    VARCHAR(1)   NOT NULL COMMENT '是否更新数据',
  requests_recovery VARCHAR(1)   NOT NULL COMMENT '是否接受恢复执行',
  job_data          BLOB         NULL COMMENT '存放持久化job对象',
  PRIMARY KEY (sched_name, job_name, job_group)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务详细信息表';

CREATE TABLE QRTZ_TRIGGERS (
  sched_name     VARCHAR(120) NOT NULL COMMENT '调度名称',
  trigger_name   VARCHAR(200) NOT NULL COMMENT '触发器名称',
  trigger_group  VARCHAR(200) NOT NULL COMMENT '触发器组名',
  job_name       VARCHAR(200) NOT NULL COMMENT '任务名称',
  job_group      VARCHAR(200) NOT NULL COMMENT '任务组名',
  description    VARCHAR(250) NULL COMMENT '相关介绍',
  next_fire_time BIGINT(13)   NULL COMMENT '下次触发时间',
  prev_fire_time BIGINT(13)   NULL COMMENT '上次触发时间',
  priority       INTEGER      NULL COMMENT '优先级',
  trigger_state  VARCHAR(16)  NOT NULL COMMENT '触发器状态',
  trigger_type   VARCHAR(8)   NOT NULL COMMENT '触发器类型',
  start_time     BIGINT(13)   NOT NULL COMMENT '开始时间',
  end_time       BIGINT(13)   NULL COMMENT '结束时间',
  calendar_name  VARCHAR(200) NULL COMMENT '日历名称',
  misfire_instr  SMALLINT     NULL COMMENT '补偿执行的策略',
  job_data       BLOB         NULL COMMENT '存放持久化job对象',
  PRIMARY KEY (sched_name, trigger_name, trigger_group),
  FOREIGN KEY (sched_name, job_name, job_group) REFERENCES QRTZ_JOB_DETAILS(sched_name, job_name, job_group)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='触发器详细信息表';

CREATE TABLE QRTZ_SIMPLE_TRIGGERS (
  sched_name      VARCHAR(120) NOT NULL COMMENT '调度名称',
  trigger_name    VARCHAR(200) NOT NULL COMMENT '触发器名称',
  trigger_group   VARCHAR(200) NOT NULL COMMENT '触发器组名',
  repeat_count    BIGINT(7)    NOT NULL COMMENT '重复次数',
  repeat_interval BIGINT(12)   NOT NULL COMMENT '重复间隔',
  times_triggered BIGINT(10)   NOT NULL COMMENT '已触发次数',
  PRIMARY KEY (sched_name, trigger_name, trigger_group),
  FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES QRTZ_TRIGGERS(sched_name, trigger_name, trigger_group)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简单触发器表';

CREATE TABLE QRTZ_CRON_TRIGGERS (
  sched_name      VARCHAR(120) NOT NULL COMMENT '调度名称',
  trigger_name    VARCHAR(200) NOT NULL COMMENT '触发器名称',
  trigger_group   VARCHAR(200) NOT NULL COMMENT '触发器组名',
  cron_expression VARCHAR(200) NOT NULL COMMENT 'cron表达式',
  time_zone_id    VARCHAR(80)  NULL COMMENT '时区',
  PRIMARY KEY (sched_name, trigger_name, trigger_group),
  FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES QRTZ_TRIGGERS(sched_name, trigger_name, trigger_group)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Cron触发器表';

CREATE TABLE QRTZ_SIMPROP_TRIGGERS (
  sched_name      VARCHAR(120)   NOT NULL COMMENT '调度名称',
  trigger_name    VARCHAR(200)   NOT NULL COMMENT '触发器名称',
  trigger_group   VARCHAR(200)   NOT NULL COMMENT '触发器组名',
  str_prop_1      VARCHAR(512)   NULL,
  str_prop_2      VARCHAR(512)   NULL,
  str_prop_3      VARCHAR(512)   NULL,
  int_prop_1      INT            NULL,
  int_prop_2      INT            NULL,
  long_prop_1     BIGINT         NULL,
  long_prop_2     BIGINT         NULL,
  dec_prop_1      NUMERIC(13,4)  NULL,
  dec_prop_2      NUMERIC(13,4)  NULL,
  bool_prop_1     VARCHAR(1)     NULL,
  bool_prop_2     VARCHAR(1)     NULL,
  PRIMARY KEY (sched_name, trigger_name, trigger_group),
  FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES QRTZ_TRIGGERS(sched_name, trigger_name, trigger_group)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简单属性触发器表';

CREATE TABLE QRTZ_BLOB_TRIGGERS (
  sched_name    VARCHAR(120) NOT NULL COMMENT '调度名称',
  trigger_name  VARCHAR(200) NOT NULL COMMENT '触发器名称',
  trigger_group VARCHAR(200) NOT NULL COMMENT '触发器组名',
  blob_data     BLOB         NULL,
  PRIMARY KEY (sched_name, trigger_name, trigger_group),
  FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES QRTZ_TRIGGERS(sched_name, trigger_name, trigger_group)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Blob触发器表';

CREATE TABLE QRTZ_CALENDARS (
  sched_name    VARCHAR(120) NOT NULL COMMENT '调度名称',
  calendar_name VARCHAR(200) NOT NULL COMMENT '日历名称',
  calendar      BLOB         NOT NULL COMMENT '存放持久化calendar对象',
  PRIMARY KEY (sched_name, calendar_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日历信息表';

CREATE TABLE QRTZ_PAUSED_TRIGGER_GRPS (
  sched_name    VARCHAR(120) NOT NULL COMMENT '调度名称',
  trigger_group VARCHAR(200) NOT NULL COMMENT '触发器组名',
  PRIMARY KEY (sched_name, trigger_group)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='暂停的触发器组表';

CREATE TABLE QRTZ_FIRED_TRIGGERS (
  sched_name        VARCHAR(120) NOT NULL COMMENT '调度名称',
  entry_id          VARCHAR(95)  NOT NULL COMMENT '调度实例id',
  trigger_name      VARCHAR(200) NOT NULL COMMENT '触发器名称',
  trigger_group     VARCHAR(200) NOT NULL COMMENT '触发器组名',
  instance_name     VARCHAR(200) NOT NULL COMMENT '调度实例名称',
  fired_time        BIGINT(13)   NOT NULL COMMENT '触发时间',
  sched_time        BIGINT(13)   NOT NULL COMMENT '调度时间',
  priority          INTEGER      NOT NULL COMMENT '优先级',
  state             VARCHAR(16)  NOT NULL COMMENT '状态',
  job_name          VARCHAR(200) NULL COMMENT '任务名称',
  job_group         VARCHAR(200) NULL COMMENT '任务组名',
  is_nonconcurrent  VARCHAR(1)   NULL COMMENT '是否并发',
  requests_recovery VARCHAR(1)   NULL COMMENT '是否接受恢复执行',
  PRIMARY KEY (sched_name, entry_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='已触发的触发器表';

CREATE TABLE QRTZ_SCHEDULER_STATE (
  sched_name        VARCHAR(120) NOT NULL COMMENT '调度名称',
  instance_name     VARCHAR(200) NOT NULL COMMENT '实例名称',
  last_checkin_time BIGINT(13)   NOT NULL COMMENT '最后检查时间',
  checkin_interval  BIGINT(13)   NOT NULL COMMENT '检查间隔',
  PRIMARY KEY (sched_name, instance_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调度器状态表';

CREATE TABLE QRTZ_LOCKS (
  sched_name VARCHAR(120) NOT NULL COMMENT '调度名称',
  lock_name  VARCHAR(40)  NOT NULL COMMENT '锁名称',
  PRIMARY KEY (sched_name, lock_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='存储的悲观锁信息表';

-- ================================================================
-- 階段 3：微信小程序用戶表
-- ================================================================
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info` (
  `id`          bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id`     varchar(64) NOT NULL COMMENT '平台用户id',
  `user_name`   varchar(64) NOT NULL DEFAULT '微信用户' COMMENT '用户名',
  `user_type`   varchar(64) NOT NULL DEFAULT '1' COMMENT '用户类型',
  `phone`       varchar(64) NULL COMMENT '手机号',
  `open_id`     varchar(128) NULL COMMENT '微信用户唯一标识',
  `union_id`    varchar(128) NULL COMMENT '微信全平台用户唯一标识',
  `avatar_url`  varchar(256) NULL DEFAULT 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0' COMMENT '用户头像',
  `create_time` datetime NULL COMMENT '创建时间',
  `update_time` datetime NULL COMMENT '更新时间',
  `del_flag`    char(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  INDEX `user_info_index_user_id`(`user_id`) USING BTREE,
  INDEX `user_info_index_open_id`(`open_id`) USING BTREE,
  INDEX `user_info_index_phone`(`phone`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- 用戶註銷限制表
DROP TABLE IF EXISTS `user_account_cancel_record`;
CREATE TABLE `user_account_cancel_record` (
  `id`                  bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `app_id`              varchar(64) NOT NULL COMMENT 'C端小程序AppID',
  `open_id_hash`        varchar(128) NOT NULL COMMENT '微信OpenID的SHA-256哈希',
  `user_id`             varchar(64) NOT NULL COMMENT '注销前平台用户id',
  `cancel_time`         datetime NOT NULL COMMENT '注销时间',
  `allow_register_time` datetime NOT NULL COMMENT '允许重新注册时间',
  `create_time`         datetime DEFAULT NULL COMMENT '创建时间',
  `update_time`         datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_open_hash` (`app_id`, `open_id_hash`),
  KEY `idx_allow_register_time` (`allow_register_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='小程序用户注销重新注册限制记录';

-- ================================================================
-- 階段 4：商城業務表（30張 — 來自 project-docs/test-sql/all_in_one.sql）
-- ================================================================

-- 4.1 商家主表
DROP TABLE IF EXISTS `merchant`;
CREATE TABLE `merchant` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商家ID',
  `distributor_id` BIGINT DEFAULT NULL COMMENT '所属分销商ID',
  `name` VARCHAR(100) NOT NULL COMMENT '商家名称',
  `logo` VARCHAR(255) DEFAULT '' COMMENT '商家Logo',
  `contact` VARCHAR(50) DEFAULT '' COMMENT '联系人',
  `phone` VARCHAR(20) DEFAULT '' COMMENT '联系电话',
  `commission_rate` DECIMAL(5,2) DEFAULT 10.00 COMMENT '抽成比例(%),后台动态调整',
  `status` TINYINT DEFAULT 1 COMMENT '状态(0禁用 1正常 2待审核历史兼容 3停止合作)',
  `balance` DECIMAL(12,2) DEFAULT 0.00 COMMENT '账户余额',
  `total_income` DECIMAL(12,2) DEFAULT 0.00 COMMENT '总收入',
  `address` VARCHAR(255) DEFAULT '' COMMENT '地址',
  `avatar` VARCHAR(255) DEFAULT '' COMMENT '商家头像/封面',
  `description` VARCHAR(500) DEFAULT '' COMMENT '商家简介',
  `business_hours` VARCHAR(100) DEFAULT '' COMMENT '营业时间(如 09:00-22:00)',
  `support_refund` TINYINT DEFAULT 1 COMMENT '是否支持退款(0否 1是)',
  `support_booking` TINYINT DEFAULT 1 COMMENT '是否支持预约(0否 1是)',
  `product_count` INT DEFAULT 0 COMMENT '商品数量',
  `store_count` INT DEFAULT 0 COMMENT '门店数量',
  `c_mini_app_id` VARCHAR(64) DEFAULT NULL COMMENT 'C端小程序AppID',
  `c_mini_app_secret` VARCHAR(128) DEFAULT NULL COMMENT 'C端小程序Secret',
  `m_mini_app_id` VARCHAR(64) DEFAULT NULL COMMENT '商家端小程序AppID',
  `m_mini_app_secret` VARCHAR(128) DEFAULT NULL COMMENT '商家端小程序Secret',
  `wx_pay_mch_id` VARCHAR(64) DEFAULT NULL COMMENT '微信商户号',
  `wx_pay_api_key` VARCHAR(128) DEFAULT NULL COMMENT '微信支付API密钥',
  `receiver_openid` VARCHAR(128) DEFAULT NULL COMMENT '收款微信openid',
  `receiver_type` VARCHAR(32) DEFAULT 'WECHAT_BALANCE' COMMENT '收款账户类型',
  `map_claim_status` VARCHAR(32) DEFAULT 'NOT_CLAIMED' COMMENT '腾讯地图认领状态',
  `map_poi_id` VARCHAR(128) DEFAULT NULL COMMENT '腾讯地图POI ID',
  `map_claim_url` VARCHAR(500) DEFAULT NULL COMMENT '腾讯地图认领或门店链接',
  `map_claim_time` DATETIME DEFAULT NULL COMMENT '腾讯地图认领完成时间',
  `map_claim_remark` VARCHAR(500) DEFAULT NULL COMMENT '腾讯地图认领备注',
  `wx_applyment_id` VARCHAR(128) DEFAULT NULL COMMENT '微信特约商户进件申请单号',
  `wx_applyment_state` VARCHAR(64) DEFAULT 'NOT_SUBMITTED' COMMENT '微信进件状态',
  `wx_applyment_reject_reason` VARCHAR(1000) DEFAULT NULL COMMENT '微信进件驳回原因',
  `wx_applyment_time` DATETIME DEFAULT NULL COMMENT '微信进件提交时间',
  `wx_applyment_finish_time` DATETIME DEFAULT NULL COMMENT '微信进件完成时间',
  `wx_payment_access_type` VARCHAR(32) DEFAULT 'EXISTING_MCH' COMMENT '微信支付接入方式',
  `merchant_wx_mch_id` VARCHAR(64) DEFAULT NULL COMMENT '商家自己的微信支付商户号',
  `merchant_wx_mch_name` VARCHAR(200) DEFAULT NULL COMMENT '商家微信支付商户名称',
  `wx_profit_sharing_enabled` TINYINT DEFAULT 0 COMMENT '是否启用微信分账：0否 1是',
  `platform_receiver_mch_id` VARCHAR(64) DEFAULT NULL COMMENT '平台作为分账接收方的微信商户号',
  `distributor_receiver_mch_id` VARCHAR(64) DEFAULT NULL COMMENT '分销商作为分账接收方的微信商户号',
  `merchant_share_rate` DECIMAL(5,2) DEFAULT 100.00 COMMENT '商家留存比例，单位百分比',
  `platform_share_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT '平台分账比例，单位百分比',
  `distributor_share_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT '分销商分账比例，单位百分比',
  `profit_sharing_contract_version` VARCHAR(128) DEFAULT NULL COMMENT '分账合同版本',
  `settlement_cycle` VARCHAR(16) DEFAULT 'T1' COMMENT '到账周期，默认T1',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_distributor_id` (`distributor_id`),
  KEY `idx_merchant_wx_applyment_id` (`wx_applyment_id`),
  KEY `idx_merchant_map_claim_status` (`map_claim_status`),
  KEY `idx_merchant_wx_applyment_state` (`wx_applyment_state`),
  KEY `idx_merchant_payment_access_type` (`wx_payment_access_type`),
  KEY `idx_merchant_wx_mch_id` (`merchant_wx_mch_id`),
  KEY `idx_merchant_profit_sharing_enabled` (`wx_profit_sharing_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家主表';

-- 4.2 商家門店表
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
  `business_hours` VARCHAR(100) DEFAULT '' COMMENT '营业时间(如 08:00-22:00)',
  `avatar` VARCHAR(255) DEFAULT '' COMMENT '门店封面图',
  `status` TINYINT DEFAULT 1 COMMENT '状态(0禁用 1正常)',
  `is_main` TINYINT DEFAULT 0 COMMENT '是否主门店(0否 1是)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家门店表';

-- 4.3 商家登錄帳號表
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

-- 4.4 商品分類表
DROP TABLE IF EXISTS `product_category`;
CREATE TABLE `product_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `sort` INT DEFAULT 0 COMMENT '排序(越大越前)',
  `status` TINYINT DEFAULT 1 COMMENT '状态(0禁用 1正常)',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 4.5 團購活動表
DROP TABLE IF EXISTS `groupon_activity`;
CREATE TABLE `groupon_activity` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `distributor_id` BIGINT DEFAULT NULL COMMENT '分销商ID',
  `name` VARCHAR(100) NOT NULL COMMENT '活动名称',
  `cover_image` VARCHAR(255) DEFAULT '' COMMENT '封面图',
  `poster_image` VARCHAR(255) DEFAULT '' COMMENT '活动海报图',
  `detail_images` TEXT DEFAULT NULL COMMENT '活动详情图JSON数组',
  `description` VARCHAR(500) DEFAULT '' COMMENT '活动描述',
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME NOT NULL COMMENT '结束时间',
  `status` TINYINT DEFAULT 0 COMMENT '状态(0未开始 1进行中 2已结束)',
  `total_sold` INT DEFAULT 0 COMMENT '已售数量',
  `limit_per_user` INT DEFAULT 0 COMMENT '每人限购(0不限)',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `source_type` VARCHAR(20) DEFAULT 'ADMIN' COMMENT '创建来源：ADMIN总后台/MERCHANT商家端',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_distributor_id` (`distributor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团购活动表';

-- 4.6 商品表
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
  `groupon_id` BIGINT DEFAULT NULL COMMENT '关联团购活动ID',
  `name` VARCHAR(200) NOT NULL COMMENT '商品名称',
  `cover_image` VARCHAR(255) DEFAULT '' COMMENT '封面图',
  `images` VARCHAR(2000) DEFAULT '' COMMENT '商品图组(JSON数组)',
  `main_image` VARCHAR(500) DEFAULT NULL COMMENT '商品主图URL(冗余，快速展示用)',
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '团购价',
  `original_price` DECIMAL(10,2) DEFAULT 0.00 COMMENT '原价',
  `stock` INT DEFAULT 0 COMMENT '库存',
  `sales` INT DEFAULT 0 COMMENT '销量',
  `status` TINYINT DEFAULT 0 COMMENT '状态(0下架 1上架)',
  `verify_type` TINYINT DEFAULT 1 COMMENT '核销方式: 1在线核销 2到店自提',
  `valid_days` INT DEFAULT 30 COMMENT '购买后有效天数',
  `verify_notice` VARCHAR(500) DEFAULT '' COMMENT '核销说明',
  `description` VARCHAR(1000) DEFAULT '' COMMENT '商品描述',
  `store_ids` VARCHAR(500) DEFAULT '' COMMENT '可用门店ID列表(JSON数组)',
  `sort` INT DEFAULT 0 COMMENT '排序(越大越前)',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_groupon_id` (`groupon_id`),
  KEY `idx_status` (`status`),
  KEY `idx_verify_type` (`verify_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 4.7 商品圖片表
DROP TABLE IF EXISTS `product_image`;
CREATE TABLE `product_image` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `image_type` VARCHAR(20) NOT NULL COMMENT '图片类型: main主图/detail详情图/sku',
  `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL路径',
  `sort_order` INT DEFAULT 0 COMMENT '排序号(越小越靠前)',
  `sku_value` VARCHAR(100) DEFAULT NULL COMMENT 'SKU值(如: 红色/蓝色, 仅sku类型时填写)',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0删除 1正常',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_product_type` (`product_id`, `image_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品图片表';

-- 4.8 商城用戶表
DROP TABLE IF EXISTS `mall_user`;
CREATE TABLE `mall_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `nickname` VARCHAR(64) DEFAULT '' COMMENT '用户昵称',
  `phone` VARCHAR(20) DEFAULT '' COMMENT '手机号',
  `avatar` VARCHAR(255) DEFAULT '' COMMENT '头像',
  `gender` TINYINT DEFAULT 0 COMMENT '性别(0未知 1男 2女)',
  `city` VARCHAR(50) DEFAULT '' COMMENT '城市',
  `open_id` VARCHAR(64) DEFAULT '' COMMENT '微信openId',
  `status` TINYINT DEFAULT 1 COMMENT '状态(0禁用 1正常)',
  `total_orders` INT DEFAULT 0 COMMENT '总订单数',
  `total_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '总消费金额',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_open_id` (`open_id`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商城用户表';

-- 4.9 優惠券模板表
DROP TABLE IF EXISTS `coupon`;
CREATE TABLE `coupon` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
  `merchant_id` BIGINT DEFAULT NULL COMMENT '商家ID(NULL为平台券)',
  `name` VARCHAR(100) NOT NULL COMMENT '优惠券名称',
  `type` TINYINT NOT NULL COMMENT '类型(1满减 2折扣 3代金券)',
  `discount_value` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠值(满减金额或折扣比例)',
  `min_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '使用门槛(最低消费)',
  `total_count` INT DEFAULT 0 COMMENT '发放总量',
  `used_count` INT DEFAULT 0 COMMENT '已使用数量',
  `start_time` DATETIME NOT NULL COMMENT '生效时间',
  `end_time` DATETIME NOT NULL COMMENT '失效时间',
  `status` TINYINT DEFAULT 1 COMMENT '状态(0禁用 1正常)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券模板表';

-- 4.10 用戶優惠券表
DROP TABLE IF EXISTS `user_coupon`;
CREATE TABLE `user_coupon` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `coupon_id` BIGINT NOT NULL COMMENT '优惠券ID',
  `merchant_id` BIGINT DEFAULT NULL COMMENT '商家ID',
  `status` TINYINT DEFAULT 0 COMMENT '状态(0未使用 1已使用 2已过期)',
  `use_time` DATETIME DEFAULT NULL COMMENT '使用时间',
  `order_no` VARCHAR(32) DEFAULT '' COMMENT '关联订单号',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_coupon_id` (`coupon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

-- 4.11 用戶收藏表
DROP TABLE IF EXISTS `user_favorite`;
CREATE TABLE `user_favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `target_type` TINYINT NOT NULL COMMENT '收藏类型(1商品 2商家 3活动)',
  `target_id` BIGINT NOT NULL COMMENT '目标ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';

-- 4.12 用戶收貨地址表
DROP TABLE IF EXISTS `user_address`;
CREATE TABLE `user_address` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `name` VARCHAR(50) NOT NULL COMMENT '收货人',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `province` VARCHAR(50) DEFAULT '' COMMENT '省',
  `city` VARCHAR(50) DEFAULT '' COMMENT '市',
  `district` VARCHAR(50) DEFAULT '' COMMENT '区',
  `detail` VARCHAR(255) DEFAULT '' COMMENT '详细地址',
  `is_default` TINYINT DEFAULT 0 COMMENT '是否默认(0否 1是)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收货地址表';

-- 4.13 訂單主表
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
  `status` TINYINT DEFAULT 0 COMMENT '状态(0待支付 1已支付 2已使用 3已完成 4已退款 5已取消)',
  `write_off_code` VARCHAR(32) DEFAULT '' COMMENT '核销码',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `use_time` DATETIME DEFAULT NULL COMMENT '使用/核销时间',
  `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
  `refund_time` DATETIME DEFAULT NULL COMMENT '退款时间',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_status` (`status`),
  UNIQUE KEY `uk_write_off_code` (`write_off_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 4.14 訂單商品明細表
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

-- 4.15 資金流水表
DROP TABLE IF EXISTS `transaction_record`;
CREATE TABLE `transaction_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '流水ID',
  `merchant_id` BIGINT DEFAULT NULL COMMENT '商家ID(NULL则为平台级)',
  `type` VARCHAR(20) NOT NULL COMMENT '类型(payment/income/withdraw/refund/commission)',
  `amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '金额',
  `balance` DECIMAL(12,2) DEFAULT 0.00 COMMENT '变动后余额',
  `order_no` VARCHAR(32) DEFAULT '' COMMENT '关联订单号',
  `description` VARCHAR(255) DEFAULT '' COMMENT '描述',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_type` (`type`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资金流水表';

-- 4.16 提現記錄表
DROP TABLE IF EXISTS `withdraw_record`;
CREATE TABLE `withdraw_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '提现金额',
  `bank_name` VARCHAR(100) DEFAULT '' COMMENT '银行名称',
  `bank_account` VARCHAR(50) DEFAULT '' COMMENT '银行账号',
  `account_name` VARCHAR(50) DEFAULT '' COMMENT '开户名',
  `status` TINYINT DEFAULT 0 COMMENT '状态(0待审核 1审核通过 2已打款 3拒绝)',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `pay_time` DATETIME DEFAULT NULL COMMENT '打款时间',
  `reject_reason` VARCHAR(255) DEFAULT '' COMMENT '拒绝原因',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提现记录表';

-- 4.17 平台收益表
DROP TABLE IF EXISTS `platform_income`;
CREATE TABLE `platform_income` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号',
  `order_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '订单金额',
  `commission_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT '抽成比例(%)',
  `commission` DECIMAL(10,2) DEFAULT 0.00 COMMENT '佣金金额',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台收益表';

-- 4.18 商家賬單表
DROP TABLE IF EXISTS `merchant_bill`;
CREATE TABLE `merchant_bill` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `bill_no` VARCHAR(32) NOT NULL COMMENT '账单编号',
  `bill_type` VARCHAR(20) DEFAULT 'daily' COMMENT '账单类型(daily/weekly/monthly)',
  `start_date` DATE DEFAULT NULL COMMENT '账单开始日期',
  `end_date` DATE DEFAULT NULL COMMENT '账单结束日期',
  `total_orders` INT DEFAULT 0 COMMENT '总订单数',
  `total_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '总金额',
  `total_commission` DECIMAL(12,2) DEFAULT 0.00 COMMENT '总佣金',
  `net_income` DECIMAL(12,2) DEFAULT 0.00 COMMENT '净收入',
  `status` TINYINT DEFAULT 0 COMMENT '状态(0待结算 1已结算)',
  `settle_time` DATETIME DEFAULT NULL COMMENT '结算时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bill_no` (`bill_no`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家账单表';

-- 4.19 操作日誌表（通用）
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `operator` VARCHAR(64) DEFAULT '' COMMENT '操作人',
  `operator_type` VARCHAR(20) DEFAULT 'admin' COMMENT '操作人类型(admin/merchant)',
  `module` VARCHAR(50) DEFAULT '' COMMENT '模块名',
  `action` VARCHAR(100) DEFAULT '' COMMENT '操作动作',
  `method` VARCHAR(200) DEFAULT '' COMMENT '方法名',
  `request_param` VARCHAR(2000) DEFAULT '' COMMENT '请求参数',
  `ip` VARCHAR(50) DEFAULT '' COMMENT '操作IP',
  `status` TINYINT DEFAULT 0 COMMENT '状态(0成功 1失败)',
  `error_msg` VARCHAR(2000) DEFAULT '' COMMENT '错误消息',
  `cost_time` INT DEFAULT 0 COMMENT '耗时(毫秒)',
  `oper_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_operator` (`operator`),
  KEY `idx_oper_time` (`oper_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 4.20 登錄日誌表(運營後台)
DROP TABLE IF EXISTS `mall_login_log`;
CREATE TABLE `mall_login_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_name` VARCHAR(64) DEFAULT '' COMMENT '用户账号',
  `ip` VARCHAR(50) DEFAULT '' COMMENT '登录IP',
  `location` VARCHAR(100) DEFAULT '' COMMENT '登录地点',
  `browser` VARCHAR(100) DEFAULT '' COMMENT '浏览器',
  `os` VARCHAR(100) DEFAULT '' COMMENT '操作系统',
  `status` TINYINT DEFAULT 0 COMMENT '状态(0成功 1失败)',
  `msg` VARCHAR(255) DEFAULT '' COMMENT '提示消息',
  `login_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_user_name` (`user_name`),
  KEY `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表(运营后台)';

-- 4.21 操作日誌表(運營後台)
DROP TABLE IF EXISTS `mall_oper_log`;
CREATE TABLE `mall_oper_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `operator` VARCHAR(64) DEFAULT '' COMMENT '操作人',
  `module` VARCHAR(50) DEFAULT '' COMMENT '模块名',
  `operation` VARCHAR(100) DEFAULT '' COMMENT '操作内容',
  `method` VARCHAR(200) DEFAULT '' COMMENT '方法名',
  `request_param` VARCHAR(2000) DEFAULT '' COMMENT '请求参数',
  `ip` VARCHAR(50) DEFAULT '' COMMENT '操作IP',
  `status` TINYINT DEFAULT 0 COMMENT '状态(0成功 1失败)',
  `error_msg` VARCHAR(2000) DEFAULT '' COMMENT '错误消息',
  `cost_time` INT DEFAULT 0 COMMENT '耗时(毫秒)',
  `oper_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_operator` (`operator`),
  KEY `idx_oper_time` (`oper_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表(运营后台)';

-- 4.22 購物車表
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
  `checked` TINYINT DEFAULT 1 COMMENT '是否选中(0否 1是)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 4.23 首頁輪播圖表
DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `title` VARCHAR(100) DEFAULT '' COMMENT '标题',
  `image` VARCHAR(255) NOT NULL COMMENT '图片URL',
  `link_type` TINYINT DEFAULT 0 COMMENT '跳转类型(0不跳转 1商品 2活动 3商家 4外链)',
  `link_id` BIGINT DEFAULT NULL COMMENT '关联ID(商品/活动/商家ID)',
  `link_url` VARCHAR(500) DEFAULT '' COMMENT '外链URL',
  `sort` INT DEFAULT 0 COMMENT '排序(越大越前)',
  `status` TINYINT DEFAULT 1 COMMENT '状态(0禁用 1启用)',
  `position` VARCHAR(50) DEFAULT 'home' COMMENT '位置(home首页/coupon优惠券页)',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_position` (`position`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页轮播图/推荐位表';

-- 4.24 支付記錄表
DROP TABLE IF EXISTS `payment_record`;
CREATE TABLE `payment_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '支付金额',
  `pay_type` VARCHAR(20) DEFAULT 'wechat' COMMENT '支付方式(wechat/alipay)',
  `transaction_id` VARCHAR(64) DEFAULT '' COMMENT '微信支付交易号',
  `out_trade_no` VARCHAR(64) DEFAULT '' COMMENT '商户订单号',
  `pay_status` TINYINT DEFAULT 0 COMMENT '支付状态(0待支付 1成功 2失败 3已关闭)',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付完成时间',
  `notify_result` TEXT DEFAULT NULL COMMENT '支付/退款回调结果摘要或原始报文',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `active_order_no` VARCHAR(64) GENERATED ALWAYS AS (CASE WHEN `del_flag` = '0' THEN `order_no` ELSE NULL END) STORED COMMENT '有效订单号唯一约束辅助列',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_record_active_order_no` (`active_order_no`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_transaction_id` (`transaction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- 4.25 退款記錄表
DROP TABLE IF EXISTS `refund_record`;
CREATE TABLE `refund_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '原订单编号',
  `refund_no` VARCHAR(64) NOT NULL COMMENT '退款单号',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `payment_record_id` BIGINT DEFAULT NULL COMMENT '关联支付记录ID',
  `refund_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '退款金额',
  `refund_reason` VARCHAR(255) DEFAULT '' COMMENT '退款原因',
  `refund_type` TINYINT DEFAULT 1 COMMENT '退款类型(1用户申请 2平台操作 3超时自动)',
  `status` TINYINT DEFAULT 0 COMMENT '状态(0待审核 1通过 2退款中 3已退款 4拒绝)',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `refund_time` DATETIME DEFAULT NULL COMMENT '退款完成时间',
  `reject_reason` VARCHAR(255) DEFAULT '' COMMENT '拒绝原因',
  `operator` VARCHAR(64) DEFAULT '' COMMENT '操作人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款记录表';

-- 4.26 核銷記錄表
DROP TABLE IF EXISTS `write_off_record`;
CREATE TABLE `write_off_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `write_off_code` VARCHAR(32) NOT NULL COMMENT '核销码',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `store_id` BIGINT DEFAULT NULL COMMENT '核销门店ID',
  `operator_id` BIGINT NOT NULL COMMENT '操作员ID(商家用户表ID)',
  `operator_name` VARCHAR(64) DEFAULT '' COMMENT '操作员姓名',
  `write_off_type` TINYINT DEFAULT 1 COMMENT '核销方式: 1扫码核销 2手动核销',
  `write_off_time` DATETIME NOT NULL COMMENT '核销时间',
  `product_name` VARCHAR(200) DEFAULT '' COMMENT '商品名称(冗余)',
  `product_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '商品金额(冗余)',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 1有效 0作废',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_write_off_code` (`write_off_code`),
  UNIQUE KEY `uk_write_off_record_order_no` (`order_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_write_off_time` (`write_off_time`),
  KEY `idx_merchant_time` (`merchant_id`, `write_off_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='核销记录表';

-- 4.27 團購商品明細表
DROP TABLE IF EXISTS `groupon_activity_item`;
CREATE TABLE `groupon_activity_item` (
  `id`                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id`         BIGINT       NOT NULL COMMENT '商家ID',
  `distributor_id`      BIGINT       DEFAULT NULL COMMENT '分销商ID',
  `groupon_id`          BIGINT       NOT NULL COMMENT '所属团购活动ID',
  `name`                VARCHAR(200) NOT NULL COMMENT '团购商品名称',
  `title`               VARCHAR(200) DEFAULT NULL COMMENT '展示标题',
  `content`             TEXT         DEFAULT NULL COMMENT '套餐内容/服务内容',
  `description`         VARCHAR(500) DEFAULT NULL COMMENT '商品说明',
  `cover_image`         VARCHAR(500) DEFAULT NULL COMMENT '封面图URL',
  `detail_images`       TEXT         DEFAULT NULL COMMENT '详情图JSON数组',
  `original_price`      DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '原价，单位元',
  `groupon_price`       DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '团购价/现价，单位元',
  `discount_rate`       DECIMAL(5,2) DEFAULT NULL COMMENT '折扣，如7.5表示7.5折',
  `stock`               INT          NOT NULL DEFAULT 0 COMMENT '团购库存',
  `sales`               INT          NOT NULL DEFAULT 0 COMMENT '团购销量',
  `limit_per_user`      INT          NOT NULL DEFAULT 0 COMMENT '每人限购，0不限',
  `valid_days`          INT          NOT NULL DEFAULT 30 COMMENT '购买后有效天数',
  `store_ids`           VARCHAR(500) DEFAULT NULL COMMENT '可用门店ID JSON数组',
  `dish_groups`         LONGTEXT     DEFAULT NULL COMMENT '菜品组JSON',
  `dish_total_price`    BIGINT       DEFAULT 0 COMMENT '菜品总价，单位分',
  `direct_total_price`  TINYINT      DEFAULT 0 COMMENT '是否直接设置菜品总价：0否 1是',
  `dish_count`          INT          DEFAULT 0 COMMENT '菜品数量统计',
  `available_dish_count` INT         DEFAULT 0 COMMENT '实际可享用菜品数量',
  `status`              INT          NOT NULL DEFAULT 0 COMMENT '状态：0下架 1上架',
  `sort`                INT          NOT NULL DEFAULT 0 COMMENT '排序值，越大越靠前',
  `del_flag`            CHAR(1)      DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_time`         DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`         DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_groupon_id` (`groupon_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_distributor_id` (`distributor_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团购商品明细表';

-- 4.28 分銷商表
DROP TABLE IF EXISTS `distributor`;
CREATE TABLE `distributor` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name`            VARCHAR(200) NOT NULL COMMENT '分销商名称',
  `contact`         VARCHAR(100) DEFAULT NULL COMMENT '联系人',
  `phone`           VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
  `username`        VARCHAR(100) NOT NULL COMMENT '登录账号',
  `password`        VARCHAR(200) NOT NULL COMMENT '登录密码 BCrypt',
  `region_code`     VARCHAR(50)  DEFAULT NULL COMMENT '区域编码',
  `region_name`     VARCHAR(100) DEFAULT NULL COMMENT '区域名称',
  `status`          INT          NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1正常',
  `remark`          VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分销商表';

-- 4.29 商家結算記錄表
DROP TABLE IF EXISTS `merchant_settlement_record`;
CREATE TABLE `merchant_settlement_record` (
  `id`                        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '结算记录ID',
  `settlement_no`             VARCHAR(64)  NOT NULL COMMENT '结算单号',
  `merchant_id`               BIGINT       NOT NULL COMMENT '商家ID',
  `distributor_id`            BIGINT       DEFAULT NULL COMMENT '分销商ID',
  `store_id`                  BIGINT       DEFAULT NULL COMMENT '门店ID',
  `order_no`                  VARCHAR(64)  DEFAULT NULL COMMENT '关联订单号',
  `title`                     VARCHAR(255) DEFAULT NULL COMMENT '结算标题/商品名称',
  `order_amount`              DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '订单金额，单位元',
  `merchant_amount`           DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '商家结算金额，单位元',
  `platform_fee_amount`       DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '平台佣金，单位元',
  `status`                    VARCHAR(32)  NOT NULL DEFAULT 'WAITING_T1' COMMENT 'WAITING_T1/TRANSFERRING/ARRIVED/FAILED/CANCELLED/REFUND_PROCESSING/REVERSED',
  `apply_time`                DATETIME     DEFAULT NULL COMMENT '进入结算链路时间',
  `expected_transfer_time`    DATETIME     DEFAULT NULL COMMENT '预计打款时间',
  `transfer_time`             DATETIME     DEFAULT NULL COMMENT '发起打款时间',
  `arrive_time`               DATETIME     DEFAULT NULL COMMENT '到账时间',
  `fail_reason`               VARCHAR(500) DEFAULT NULL COMMENT '失败原因',
  `wechat_batch_no`           VARCHAR(128) DEFAULT NULL COMMENT '微信批次号',
  `wechat_detail_no`          VARCHAR(128) DEFAULT NULL COMMENT '微信明细单号',
  `reverse_record_id`         BIGINT       DEFAULT NULL COMMENT '逆向/负向记录ID',
  `del_flag`                  CHAR(1)      DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_time`               DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`               DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_settlement_no` (`settlement_no`),
  KEY `idx_merchant_status` (`merchant_id`, `status`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_expected_transfer_time` (`expected_transfer_time`),
  KEY `idx_distributor_id` (`distributor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家结算记录表';

-- 4.30 訂單三方分賬流水表
DROP TABLE IF EXISTS `order_profit_ledger`;
CREATE TABLE `order_profit_ledger` (
  `id`                  BIGINT       NOT NULL AUTO_INCREMENT,
  `order_no`            VARCHAR(64)  NOT NULL COMMENT '订单号',
  `merchant_id`         BIGINT       NOT NULL COMMENT '商家ID',
  `distributor_id`      BIGINT       DEFAULT NULL COMMENT '分销商ID',
  `pay_amount`          DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '用户实付金额，单位元',
  `merchant_amount`     DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '商家应得金额，单位元',
  `platform_amount`     DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '平台抽成金额，单位元',
  `distributor_amount`  DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '分销商佣金金额，单位元',
  `merchant_rate`       DECIMAL(5,2)  DEFAULT NULL COMMENT '商家比例, 如85.00',
  `platform_rate`       DECIMAL(5,2)  DEFAULT NULL COMMENT '平台比例, 如10.00',
  `distributor_rate`    DECIMAL(5,2)  DEFAULT NULL COMMENT '分销商比例, 如5.00',
  `status`              VARCHAR(32)   NOT NULL DEFAULT 'WAITING_SETTLEMENT' COMMENT 'WAITING_SETTLEMENT/SETTLED/REFUND_REVERSED',
  `finish_time`         DATETIME      DEFAULT NULL COMMENT '订单完成时间',
  `del_flag`            CHAR(1)       DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_time`         DATETIME      DEFAULT CURRENT_TIMESTAMP,
  `update_time`         DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_distributor_id` (`distributor_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单三方分账流水表';

-- 4.31 分銷商佣金結算記錄表
DROP TABLE IF EXISTS `distributor_settlement_record`;
CREATE TABLE `distributor_settlement_record` (
  `id`                        BIGINT       NOT NULL AUTO_INCREMENT,
  `settlement_no`             VARCHAR(64)  NOT NULL COMMENT '结算单号',
  `distributor_id`            BIGINT       NOT NULL COMMENT '分销商ID',
  `merchant_id`               BIGINT       DEFAULT NULL COMMENT '关联商家ID',
  `order_no`                  VARCHAR(64)  DEFAULT NULL COMMENT '关联订单号',
  `amount`                    DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '佣金金额，单位元',
  `rate`                      DECIMAL(5,2)  DEFAULT NULL COMMENT '佣金比例',
  `status`                    VARCHAR(32)   NOT NULL DEFAULT 'WAITING_SETTLEMENT' COMMENT 'WAITING_SETTLEMENT/TRANSFERRING/ARRIVED/FAILED/CANCELLED/REVERSED',
  `settlement_period_start`   DATE          DEFAULT NULL COMMENT '结算周期开始',
  `settlement_period_end`     DATE          DEFAULT NULL COMMENT '结算周期结束',
  `expected_transfer_time`    DATETIME      DEFAULT NULL COMMENT '预计打款时间',
  `transfer_time`             DATETIME      DEFAULT NULL COMMENT '发起打款时间',
  `arrive_time`               DATETIME      DEFAULT NULL COMMENT '到账时间',
  `fail_reason`               VARCHAR(500)  DEFAULT NULL COMMENT '失败原因',
  `reverse_record_id`         BIGINT        DEFAULT NULL COMMENT '逆向记录ID',
  `del_flag`                  CHAR(1)       DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_time`               DATETIME      DEFAULT CURRENT_TIMESTAMP,
  `update_time`               DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_settlement_no` (`settlement_no`),
  KEY `idx_distributor_id` (`distributor_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_status` (`status`),
  KEY `idx_expected_transfer_time` (`expected_transfer_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分销商佣金结算记录表';

-- 4.32 平台轉帳記錄表
DROP TABLE IF EXISTS `platform_transfer_record`;
CREATE TABLE `platform_transfer_record` (
  `id`                        BIGINT       NOT NULL AUTO_INCREMENT,
  `transfer_no`               VARCHAR(64)  NOT NULL COMMENT '平台转账单号',
  `settlement_record_type`    VARCHAR(64)  NOT NULL COMMENT '关联结算类型：MERCHANT/DISTRIBUTOR',
  `settlement_record_id`      BIGINT       NOT NULL COMMENT '关联结算记录ID',
  `merchant_id`               BIGINT       DEFAULT NULL COMMENT '商家ID（收款方是商家时）',
  `distributor_id`            BIGINT       DEFAULT NULL COMMENT '分销商ID（收款方是分销商时）',
  `amount`                    DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '转账金额，单位元',
  `status`                    VARCHAR(32)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SUCCESS/FAILED',
  `transfer_time`             DATETIME     DEFAULT NULL COMMENT '发起转账时间',
  `arrive_time`               DATETIME     DEFAULT NULL COMMENT '到账时间',
  `fail_reason`               VARCHAR(500) DEFAULT NULL COMMENT '失败原因',
  `create_time`               DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`               DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transfer_no` (`transfer_no`),
  KEY `idx_settlement_record` (`settlement_record_type`, `settlement_record_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台转账记录表';

-- ================================================================
-- 階段 5：測試數據（商城主體業務）
-- 來源：project-docs/test-sql/all_in_one.sql + 02_init_data.sql
-- ================================================================

-- 5.1 商家
INSERT INTO `merchant` (`id`, `distributor_id`, `name`, `logo`, `contact`, `phone`, `commission_rate`, `status`, `balance`, `total_income`, `address`, `avatar`, `description`, `business_hours`, `product_count`, `store_count`) VALUES
(1, NULL, '鲜果园水果店', '/profile/upload/merchant/fruit_logo.png', '张三', '13800001111', 5.00, 1, 12580.50, 58320.00, '北京市朝阳区建国路88号', '/profile/upload/merchant/fruit_cover.png', '新鲜水果,产地直供,品质保证', '08:00-22:00', 6, 2),
(2, NULL, '好味烘焙坊', '/profile/upload/merchant/bakery_logo.png', '李四', '13800002222', 8.00, 1, 8960.00, 35600.00, '北京市海淀区中关村大街12号', '/profile/upload/merchant/bakery_cover.png', '手工烘焙,现做现卖,甜蜜每一天', '07:00-21:00', 5, 1),
(3, NULL, '川味小厨', '/profile/upload/merchant/chuan_logo.png', '王五', '13800003333', 12.00, 1, 5200.00, 18900.00, '北京市西城区西单北大街56号', '/profile/upload/merchant/chuan_cover.png', '正宗川菜,麻辣鲜香,地道风味', '10:00-23:00', 5, 1);
-- 如需重置自增ID，執行：ALTER TABLE merchant AUTO_INCREMENT = 4;

-- 5.2 商家登錄帳號（密碼统一 admin123 BCrypt）
INSERT INTO `merchant_user` (`id`, `merchant_id`, `username`, `password`, `real_name`, `phone`, `role`, `status`) VALUES
(1, 1, 'fruit_admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '张三', '13800001111', 'owner', 1),
(2, 1, 'fruit_staff', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '赵六', '13900001111', 'member', 1),
(3, 2, 'bakery_admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '李四', '13800002222', 'owner', 1),
(4, 3, 'chuan_admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '王五', '13800003333', 'owner', 1);

-- 5.3 門店
INSERT INTO `merchant_store` (`id`, `merchant_id`, `name`, `contact`, `phone`, `address`, `longitude`, `latitude`, `business_hours`, `avatar`, `status`, `is_main`) VALUES
(1, 1, '鲜果园(建国路总店)', '张三', '13800001111', '北京市朝阳区建国路88号', 116.4718750, 39.9150420, '08:00-22:00', '/profile/upload/store/fruit_main.jpg', 1, 1),
(2, 1, '鲜果园(望京分店)', '赵六', '13900001111', '北京市朝阳区望京西路68号', 116.4801230, 39.9904560, '08:00-22:00', '/profile/upload/store/fruit_wj.jpg', 1, 0),
(3, 2, '好味烘焙(中关村店)', '李四', '13800002222', '北京市海淀区中关村大街12号', 116.3168760, 39.9834520, '07:00-21:00', '/profile/upload/store/bakery_zgc.jpg', 1, 1),
(4, 3, '川味小厨(西单店)', '王五', '13800003333', '北京市西城区西单北大街56号', 116.3739870, 39.9123450, '10:00-23:00', '/profile/upload/store/chuan_xd.jpg', 1, 1);

-- 5.4 商品分類
INSERT INTO `product_category` (`id`, `merchant_id`, `name`, `sort`) VALUES
(1, 1, '时令水果', 100), (2, 1, '进口水果', 90), (3, 1, '水果礼盒', 80),
(4, 2, '面包蛋糕', 100), (5, 2, '甜品点心', 90), (6, 2, '饮品', 80),
(7, 3, '招牌菜', 100), (8, 3, '经典川菜', 90), (9, 3, '小吃凉菜', 80);

-- 5.5 團購活動
INSERT INTO `groupon_activity` (`id`, `merchant_id`, `name`, `cover_image`, `description`, `start_time`, `end_time`, `status`, `total_sold`, `limit_per_user`) VALUES
(1, 1, '春季水果狂欢节', '/profile/upload/groupon/spring_fruit.jpg', '春季时令水果大促,全场8折起,满100减20', '2026-04-01 00:00:00', '2026-06-30 23:59:59', 1, 356, 5),
(2, 2, '烘焙新品尝鲜周', '/profile/upload/groupon/bakery_new.jpg', '新品面包蛋糕限时特惠,第二件半价', '2026-05-01 00:00:00', '2026-05-31 23:59:59', 1, 128, 3),
(3, 3, '川味美食节', '/profile/upload/groupon/chuan_fest.jpg', '地道川菜团购特惠,套餐低至6折', '2026-05-10 00:00:00', '2026-06-10 23:59:59', 1, 89, 0);

-- 5.6 商品
INSERT INTO `product` (`id`, `merchant_id`, `category_id`, `groupon_id`, `name`, `cover_image`, `images`, `price`, `original_price`, `stock`, `sales`, `status`, `valid_days`, `description`, `store_ids`, `sort`) VALUES
(1, 1, 1, 1, '精选红富士苹果5斤装', '/profile/upload/product/apple_cover.jpg', '[\"/profile/upload/product/apple_1.jpg\",\"/profile/upload/product/apple_2.jpg\",\"/profile/upload/product/apple_3.jpg\"]', 29.90, 49.90, 200, 86, 1, 7, '山东烟台红富士,脆甜多汁', '[1,2]', 100),
(2, 1, 1, 1, '海南金煌芒果3斤装', '/profile/upload/product/mango_cover.jpg', '[\"/profile/upload/product/mango_1.jpg\",\"/profile/upload/product/mango_2.jpg\"]', 39.90, 69.90, 150, 62, 1, 7, '海南直采金煌芒果,个大核薄', '[1,2]', 90),
(3, 1, 2, 1, '智利进口车厘子1斤', '/profile/upload/product/cherry_cover.jpg', '[\"/profile/upload/product/cherry_1.jpg\",\"/profile/upload/product/cherry_2.jpg\"]', 59.90, 89.90, 80, 45, 1, 3, '智利JJ级车厘子', '[1]', 80),
(4, 1, 3, NULL, '精品水果礼盒(大)', '/profile/upload/product/box_cover.jpg', '[\"/profile/upload/product/box_1.jpg\"]', 128.00, 198.00, 50, 23, 1, 7, '精选8种时令水果', '[1,2]', 70),
(5, 1, 1, NULL, '广西百香果10个装', '/profile/upload/product/passion_cover.jpg', '[\"/profile/upload/product/passion_1.jpg\"]', 15.90, 25.90, 300, 128, 1, 10, '广西北流百香果', '[1,2]', 60),
(6, 1, 2, NULL, '泰国山竹5斤装', '/profile/upload/product/mangosteen_cover.jpg', '[\"/profile/upload/product/mangosteen_1.jpg\"]', 89.00, 139.00, 60, 15, 0, 5, '泰国进口山竹', '[1]', 50),
(7, 2, 4, 2, '招牌奶油草莓蛋糕(6寸)', '/profile/upload/product/cake_cover.jpg', '[\"/profile/upload/product/cake_1.jpg\",\"/profile/upload/product/cake_2.jpg\"]', 68.00, 98.00, 30, 42, 1, 1, '新鲜草莓+动物奶油', '[3]', 100),
(8, 2, 4, 2, '全麦核桃吐司', '/profile/upload/product/bread_cover.jpg', '[\"/profile/upload/product/bread_1.jpg\"]', 18.00, 28.00, 50, 88, 1, 3, '全麦面粉+新疆核桃', '[3]', 90),
(9, 2, 5, 2, '手工蛋黄酥6个装', '/profile/upload/product/egg_cover.jpg', '[\"/profile/upload/product/egg_1.jpg\",\"/profile/upload/product/egg_2.jpg\"]', 32.00, 48.00, 100, 56, 1, 5, '酥皮+咸蛋黄+红豆沙', '[3]', 80),
(10, 2, 6, NULL, '现磨拿铁咖啡(大杯)', '/profile/upload/product/coffee_cover.jpg', '[\"/profile/upload/product/coffee_1.jpg\"]', 22.00, 32.00, 999, 167, 1, 1, '阿拉比卡咖啡豆现磨', '[3]', 70),
(11, 2, 5, NULL, '蔓越莓曲奇饼干礼盒', '/profile/upload/product/cookie_cover.jpg', '[\"/profile/upload/product/cookie_1.jpg\"]', 45.00, 68.00, 80, 34, 1, 30, '进口黄油+蔓越莓干', '[3]', 60),
(12, 3, 7, 3, '招牌水煮鱼套餐', '/profile/upload/product/fish_cover.jpg', '[\"/profile/upload/product/fish_1.jpg\",\"/profile/upload/product/fish_2.jpg\"]', 68.00, 108.00, 50, 38, 1, 1, '鲜活草鱼,麻辣鲜香', '[4]', 100),
(13, 3, 7, 3, '麻婆豆腐套餐', '/profile/upload/product/tofu_cover.jpg', '[\"/profile/upload/product/tofu_1.jpg\"]', 28.00, 42.00, 999, 95, 1, 1, '正宗四川麻婆豆腐', '[4]', 90),
(14, 3, 8, 3, '回锅肉套餐', '/profile/upload/product/pork_cover.jpg', '[\"/profile/upload/product/pork_1.jpg\"]', 38.00, 58.00, 200, 67, 1, 1, '蒜苗回锅肉', '[4]', 80),
(15, 3, 9, NULL, '夫妻肺片', '/profile/upload/product/lung_cover.jpg', '[\"/profile/upload/product/lung_1.jpg\"]', 32.00, 48.00, 150, 43, 1, 1, '红油夫妻肺片', '[4]', 70),
(16, 3, 9, NULL, '酸辣凉粉', '/profile/upload/product/jelly_cover.jpg', '[\"/profile/upload/product/jelly_1.jpg\"]', 12.00, 18.00, 300, 112, 1, 1, '手工豌豆凉粉', '[4]', 60);

-- 5.7 商城用戶
INSERT INTO `mall_user` (`id`, `nickname`, `phone`, `avatar`, `gender`, `city`, `open_id`, `status`, `total_orders`, `total_amount`) VALUES
(1, '小明', '15000001111', 'https://thirdwx.qlogo.cn/mmopen/vi_32/abc1', 1, '北京', 'oWxUser001abc', 1, 5, 326.70),
(2, '爱吃水果的喵', '15000002222', 'https://thirdwx.qlogo.cn/mmopen/vi_32/abc2', 2, '北京', 'oWxUser002def', 1, 3, 189.80),
(3, '美食家老王', '15000003333', 'https://thirdwx.qlogo.cn/mmopen/vi_32/abc3', 1, '上海', 'oWxUser003ghi', 1, 8, 568.50),
(4, '甜品控小李', '15000004444', 'https://thirdwx.qlogo.cn/mmopen/vi_32/abc4', 2, '广州', 'oWxUser004jkl', 1, 2, 136.00),
(5, '打工人小张', '15000005555', 'https://thirdwx.qlogo.cn/mmopen/vi_32/abc5', 1, '深圳', 'oWxUser005mno', 0, 0, 0.00);

-- 5.8-5.17 完整測試數據（優惠券、收藏、地址、訂單、購物車、輪播圖、支付、流水、日誌）
-- 完整 INSERT 語句請參考 project-docs/test-sql/all_in_one.sql 第 96~1006 行
-- 此處僅作示範，正式執行請取消以下註釋或直接運行 all_in_one.sql
-- 提示: 複製 all_in_one.sql 的第 96 行至末尾粘貼到此處即可

-- ================================================================
-- 階段 6：初始系統配置（字典、配置）
-- ================================================================
INSERT INTO `sys_config` (`config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `remark`) VALUES
('用户管理-账号初始密码', 'sys.user.initPassword', '123456', 'Y', 'admin', NOW(), '初始化密码 123456'),
('主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', 'Y', 'admin', NOW(), '蓝色 skin-blue、绿色 skin-green'),
('用户管理-配户颜色', 'sys.user.initPasswordColor', '#1890ff', 'Y', 'admin', NOW(), '默认颜色'),
('商城合规-运营主体名称', 'mall.privacy.operatorName', '', 'N', 'admin', NOW(), '微信隐私保护指引中的个人信息处理者/运营主体真实名称'),
('商城合规-客服电话', 'mall.privacy.servicePhone', '', 'N', 'admin', NOW(), '微信隐私保护指引和小程序联系客服页展示的客服电话'),
('商城合规-联系邮箱', 'mall.privacy.contactEmail', '', 'N', 'admin', NOW(), '微信隐私保护指引和小程序联系客服页展示的联系邮箱'),
('商城合规-联系地址', 'mall.privacy.contactAddress', '', 'N', 'admin', NOW(), '微信隐私保护指引和小程序联系客服页展示的注册地址或常用联系地址'),
('商城合规-客服时间', 'mall.privacy.businessHoursText', '', 'N', 'admin', NOW(), '小程序联系客服页展示的客服服务时间，如工作日 09:00-18:00'),
('商城合规-个人信息权利请求说明', 'mall.privacy.rightsRequestTips', '如你对个人信息处理有查阅、复制、更正、删除、撤回授权、注销或投诉建议等需求，可通过小程序“联系客服”、订单详情页商家联系方式或微信小程序主体公示联系方式提交。', 'N', 'admin', NOW(), '隐私保护指引联系方式与用户权利请求说明');

-- ================================================================
-- 完成標記
-- ================================================================
SELECT '✅ 數據庫 ruoyi-cs 全部補全完成！共 18 張系統表 + 11 張 Quartz 表 + 32 張業務表 = 61 張表' AS completion_message;
