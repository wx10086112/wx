-- ============================================
-- 零点科技多商家SaaS团购商城 - 建表脚本
-- 数据库: ruoyi-cs
-- 架构: 单数据库多商家共表，merchant_id数据隔离
-- 执行顺序: 01 → 02
-- ============================================

USE `ruoyi-cs`;

-- 1. 商家等级表
DROP TABLE IF EXISTS `merchant_level`;
CREATE TABLE `merchant_level` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '等级ID',
  `name` VARCHAR(50) NOT NULL COMMENT '等级名称',
  `commission_rate` DECIMAL(5,2) NOT NULL DEFAULT 10.00 COMMENT '抽成比例(%)',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '等级说明',
  `merchant_count` INT DEFAULT 0 COMMENT '商家数量',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家等级表';

-- 2. 商家表
DROP TABLE IF EXISTS `merchant`;
CREATE TABLE `merchant` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商家ID',
  `name` VARCHAR(100) NOT NULL COMMENT '商家名称',
  `contact` VARCHAR(50) DEFAULT '' COMMENT '联系人',
  `phone` VARCHAR(20) DEFAULT '' COMMENT '联系电话',
  `level_id` BIGINT DEFAULT NULL COMMENT '等级ID',
  `status` TINYINT DEFAULT 1 COMMENT '状态(0禁用 1正常 2待审核)',
  `balance` DECIMAL(12,2) DEFAULT 0.00 COMMENT '账户余额',
  `total_income` DECIMAL(12,2) DEFAULT 0.00 COMMENT '总流水',
  `withdrawable_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '可提现金额',
  `frozen_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '冻结金额',
  `bank_name` VARCHAR(100) DEFAULT '' COMMENT '开户银行',
  `bank_account` VARCHAR(50) DEFAULT '' COMMENT '银行账号',
  `address` VARCHAR(255) DEFAULT '' COMMENT '商家地址',
  `avatar` VARCHAR(255) DEFAULT '' COMMENT '商家头像',
  `description` VARCHAR(500) DEFAULT '' COMMENT '商家简介',
  `product_count` INT DEFAULT 0 COMMENT '商品数量',
  `total_orders` INT DEFAULT 0 COMMENT '总订单数',
  `total_sales` DECIMAL(12,2) DEFAULT 0.00 COMMENT '总销售额',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_level_id` (`level_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家表';

-- 3. 商家账号表
DROP TABLE IF EXISTS `merchant_user`;
CREATE TABLE `merchant_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '账号ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `username` VARCHAR(50) NOT NULL COMMENT '登录账号',
  `password` VARCHAR(100) NOT NULL COMMENT '登录密码',
  `real_name` VARCHAR(50) DEFAULT '' COMMENT '真实姓名',
  `phone` VARCHAR(20) DEFAULT '' COMMENT '手机号',
  `role` VARCHAR(20) DEFAULT 'member' COMMENT '角色(owner管理员 member成员)',
  `status` TINYINT DEFAULT 1 COMMENT '状态(0禁用 1正常)',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家账号表';

-- 4. 商品分类表
DROP TABLE IF EXISTS `product_category`;
CREATE TABLE `product_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 5. 商品表
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
  `name` VARCHAR(200) NOT NULL COMMENT '商品名称',
  `cover_image` VARCHAR(255) DEFAULT '' COMMENT '封面图',
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '售价',
  `original_price` DECIMAL(10,2) DEFAULT 0.00 COMMENT '原价',
  `stock` INT DEFAULT 0 COMMENT '库存',
  `sales` INT DEFAULT 0 COMMENT '销量',
  `status` TINYINT DEFAULT 1 COMMENT '状态(0下架 1上架)',
  `description` TEXT COMMENT '商品描述',
  `valid_days` INT DEFAULT 7 COMMENT '有效天数',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 6. 用户表
DROP TABLE IF EXISTS `mall_user`;
CREATE TABLE `mall_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `nickname` VARCHAR(50) DEFAULT '' COMMENT '昵称',
  `phone` VARCHAR(20) DEFAULT '' COMMENT '手机号',
  `avatar` VARCHAR(255) DEFAULT '' COMMENT '头像',
  `open_id` VARCHAR(64) DEFAULT '' COMMENT '微信openId',
  `status` TINYINT DEFAULT 1 COMMENT '状态(0禁用 1正常)',
  `total_orders` INT DEFAULT 0 COMMENT '总订单数',
  `total_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '总消费金额',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志(0存在 2删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_open_id` (`open_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 7. 订单表
DROP TABLE IF EXISTS `mall_order`;
CREATE TABLE `mall_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
  `total_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '订单总金额',
  `pay_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '实付金额',
  `commission` DECIMAL(10,2) DEFAULT 0.00 COMMENT '平台抽成',
  `merchant_income` DECIMAL(10,2) DEFAULT 0.00 COMMENT '商家收入',
  `status` TINYINT DEFAULT 0 COMMENT '状态(0待支付 1已支付 2已使用 3已完成 4已退款 5已取消)',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
  `write_off_code` VARCHAR(20) DEFAULT '' COMMENT '核销码',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 8. 订单商品明细表
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `product_id` BIGINT DEFAULT NULL COMMENT '商品ID',
  `product_name` VARCHAR(200) DEFAULT '' COMMENT '商品名称',
  `cover_image` VARCHAR(255) DEFAULT '' COMMENT '商品图片',
  `price` DECIMAL(10,2) DEFAULT 0.00 COMMENT '单价',
  `quantity` INT DEFAULT 1 COMMENT '数量',
  `subtotal` DECIMAL(10,2) DEFAULT 0.00 COMMENT '小计',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单商品明细表';

-- 9. 资金流水表
DROP TABLE IF EXISTS `transaction_record`;
CREATE TABLE `transaction_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '流水ID',
  `merchant_id` BIGINT DEFAULT NULL COMMENT '商家ID(平台级为NULL)',
  `type` VARCHAR(20) NOT NULL COMMENT '类型(payment支付 income收入 withdraw提现 refund退款 commission抽成)',
  `amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '金额',
  `balance` DECIMAL(12,2) DEFAULT 0.00 COMMENT '余额快照',
  `order_no` VARCHAR(32) DEFAULT '' COMMENT '关联订单号',
  `remark` VARCHAR(255) DEFAULT '' COMMENT '说明',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志(0存在 2删除)',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_type` (`type`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资金流水表';

-- 10. 提现记录表
DROP TABLE IF EXISTS `withdraw_record`;
CREATE TABLE `withdraw_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '提现ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '提现金额',
  `bank_name` VARCHAR(100) DEFAULT '' COMMENT '开户银行',
  `bank_account` VARCHAR(50) DEFAULT '' COMMENT '银行账号',
  `status` TINYINT DEFAULT 0 COMMENT '状态(0待审核 1审核通过 2已打款 3已拒绝)',
  `reject_reason` VARCHAR(255) DEFAULT '' COMMENT '拒绝原因',
  `operator` VARCHAR(64) DEFAULT '' COMMENT '审核人',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `complete_time` DATETIME DEFAULT NULL COMMENT '打款完成时间',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提现记录表';

-- 11. 平台收益表
DROP TABLE IF EXISTS `platform_income`;
CREATE TABLE `platform_income` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收益ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `order_no` VARCHAR(32) DEFAULT '' COMMENT '关联订单号',
  `order_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '订单金额',
  `commission_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT '抽成比例(%)',
  `commission` DECIMAL(10,2) DEFAULT 0.00 COMMENT '抽成金额',
  `income_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收益时间',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志(0存在 2删除)',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台收益表';

-- 12. 商家账单表
DROP TABLE IF EXISTS `merchant_bill`;
CREATE TABLE `merchant_bill` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '账单ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `bill_no` VARCHAR(32) NOT NULL COMMENT '账单编号',
  `bill_type` VARCHAR(10) DEFAULT 'monthly' COMMENT '账单类型(daily日 weekly周 monthly月)',
  `start_date` DATE DEFAULT NULL COMMENT '开始日期',
  `end_date` DATE DEFAULT NULL COMMENT '结束日期',
  `total_orders` INT DEFAULT 0 COMMENT '订单总数',
  `total_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '订单总金额',
  `total_commission` DECIMAL(12,2) DEFAULT 0.00 COMMENT '总抽成',
  `net_income` DECIMAL(12,2) DEFAULT 0.00 COMMENT '净收入',
  `status` TINYINT DEFAULT 0 COMMENT '状态(0待结算 1已结算)',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bill_no` (`bill_no`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家账单表';

-- 13. 操作日志表
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `operator` VARCHAR(64) DEFAULT '' COMMENT '操作人',
  `operator_type` VARCHAR(10) DEFAULT 'admin' COMMENT '操作人类型(admin管理员 merchant商家)',
  `merchant_id` BIGINT DEFAULT NULL COMMENT '商家ID',
  `module` VARCHAR(50) DEFAULT '' COMMENT '模块',
  `action` VARCHAR(100) DEFAULT '' COMMENT '操作描述',
  `method` VARCHAR(200) DEFAULT '' COMMENT '方法名',
  `params` TEXT COMMENT '请求参数',
  `ip` VARCHAR(50) DEFAULT '' COMMENT '操作IP',
  `status` TINYINT DEFAULT 1 COMMENT '状态(0失败 1成功)',
  `cost_time` BIGINT DEFAULT 0 COMMENT '耗时(毫秒)',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_operator_type` (`operator_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
