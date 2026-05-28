-- ============================================
-- 零点科技多商家SaaS团购商城 - 建表脚本(完整版V3)
-- 数据库: ruoyi-cs
-- 执行顺序: 01_create_tables → 02_init_data
-- 更新: 2026-05-11 合并ALTER修改,新增购物车/轮播图/支付记录/退款记录4张表,删除商家等级表
-- 共30张业务表
-- ============================================

USE `ruoyi-cs`;

-- ============================================================
-- 30张业务表
-- ============================================================

-- 1. 商家主表（含抽成比例、营业时间、门店数量）
DROP TABLE IF EXISTS `merchant`;
CREATE TABLE `merchant` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商家ID',
  `distributor_id` BIGINT DEFAULT NULL COMMENT '所属分销商ID',
  `name` VARCHAR(100) NOT NULL COMMENT '商家名称',
  `logo` VARCHAR(255) DEFAULT '' COMMENT '商家Logo',
  `contact` VARCHAR(50) DEFAULT '' COMMENT '联系人',
  `phone` VARCHAR(20) DEFAULT '' COMMENT '联系电话',
  `commission_rate` DECIMAL(5,2) DEFAULT 10.00 COMMENT '抽成比例(%),后台动态调整',
  `status` TINYINT DEFAULT 2 COMMENT '状态(0禁用 1正常 2待审核)',
  `balance` DECIMAL(12,2) DEFAULT 0.00 COMMENT '账户余额',
  `total_income` DECIMAL(12,2) DEFAULT 0.00 COMMENT '总收入',
  `address` VARCHAR(255) DEFAULT '' COMMENT '地址',
  `avatar` VARCHAR(255) DEFAULT '' COMMENT '商家头像/封面',
  `description` VARCHAR(500) DEFAULT '' COMMENT '商家简介',
  `business_hours` VARCHAR(100) DEFAULT '' COMMENT '营业时间(如 09:00-22:00)',
  `product_count` INT DEFAULT 0 COMMENT '商品数量',
  `store_count` INT DEFAULT 0 COMMENT '门店数量',
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
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
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
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME NOT NULL COMMENT '结束时间',
  `status` TINYINT DEFAULT 0 COMMENT '状态(0未开始 1进行中 2已结束)',
  `total_sold` INT DEFAULT 0 COMMENT '已售数量',
  `limit_per_user` INT DEFAULT 0 COMMENT '每人限购(0不限)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团购活动表';

-- 6. 商品表（含商品图组、团购关联、可用门店）
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
  `groupon_id` BIGINT DEFAULT NULL COMMENT '关联团购活动ID',
  `name` VARCHAR(200) NOT NULL COMMENT '商品名称',
  `cover_image` VARCHAR(255) DEFAULT '' COMMENT '封面图',
  `images` VARCHAR(2000) DEFAULT '' COMMENT '商品图组(JSON数组)',
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '团购价',
  `original_price` DECIMAL(10,2) DEFAULT 0.00 COMMENT '原价',
  `stock` INT DEFAULT 0 COMMENT '库存',
  `sales` INT DEFAULT 0 COMMENT '销量',
  `status` TINYINT DEFAULT 0 COMMENT '状态(0下架 1上架)',
  `valid_days` INT DEFAULT 30 COMMENT '购买后有效天数',
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
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 7. 商城用户表（含性别、城市）
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

-- 8. 优惠券模板表
DROP TABLE IF EXISTS `coupon`;
CREATE TABLE `coupon` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
  `merchant_id` BIGINT DEFAULT NULL COMMENT '商家ID(NULL为平台券)',
  `name` VARCHAR(100) NOT NULL COMMENT '优惠券名称',
  `type` TINYINT NOT NULL COMMENT '类型(1满减 2折扣)',
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

-- 9. 用户优惠券表
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

-- 10. 用户收藏表
DROP TABLE IF EXISTS `user_favorite`;
CREATE TABLE `user_favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `target_type` TINYINT NOT NULL COMMENT '收藏类型(1商品 2商家)',
  `target_id` BIGINT NOT NULL COMMENT '目标ID(商品ID或商家ID)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';

-- 11. 用户收货地址表
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

-- 12. 订单表（含门店核销、优惠券、团购、退款时间）
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
  KEY `idx_write_off_code` (`write_off_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 13. 订单商品明细表
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

-- 14. 资金流水表
DROP TABLE IF EXISTS `transaction_record`;
CREATE TABLE `transaction_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '流水ID',
  `merchant_id` BIGINT DEFAULT NULL COMMENT '商家ID(NULL则为平台级)',
  `type` VARCHAR(20) NOT NULL COMMENT '类型(payment支付/income收入/withdraw提现/refund退款/commission佣金)',
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

-- 15. 提现记录表
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

-- 16. 平台收益表
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

-- 17. 商家账单表
DROP TABLE IF EXISTS `merchant_bill`;
CREATE TABLE `merchant_bill` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `bill_no` VARCHAR(32) NOT NULL COMMENT '账单编号',
  `bill_type` VARCHAR(20) DEFAULT 'daily' COMMENT '账单类型(daily日账单/weekly周账单/monthly月账单)',
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

-- 18. 操作日志表
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `operator` VARCHAR(64) DEFAULT '' COMMENT '操作人',
  `operator_type` VARCHAR(20) DEFAULT 'admin' COMMENT '操作人类型(admin管理员/merchant商家)',
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

-- 19. 登录日志表(运营后台)
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

-- 20. 操作日志表(运营后台)
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

-- ============================================================
-- 新增4张业务表
-- ============================================================

-- 21. 购物车表
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

-- 22. 首页轮播图/推荐位表
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

-- 23. 支付记录表
DROP TABLE IF EXISTS `payment_record`;
CREATE TABLE `payment_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '支付金额',
  `pay_type` VARCHAR(20) DEFAULT 'wechat' COMMENT '支付方式(wechat微信/alipay支付宝)',
  `transaction_id` VARCHAR(64) DEFAULT '' COMMENT '微信支付交易号',
  `out_trade_no` VARCHAR(64) DEFAULT '' COMMENT '商户订单号',
  `pay_status` TINYINT DEFAULT 0 COMMENT '支付状态(0待支付 1支付成功 2支付失败 3已关闭)',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付完成时间',
  `notify_result` VARCHAR(50) DEFAULT '' COMMENT '回调结果(success/fail)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_transaction_id` (`transaction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- 24. 退款记录表
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
  `status` TINYINT DEFAULT 0 COMMENT '状态(0待审核 1审核通过 2退款中 3已退款 4拒绝)',
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

-- 25. 核销记录表
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
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_write_off_time` (`write_off_time`),
  KEY `idx_merchant_time` (`merchant_id`, `write_off_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='核销记录表';

-- 26. 团购商品明细表
DROP TABLE IF EXISTS `groupon_activity_item`;
CREATE TABLE `groupon_activity_item` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id`     BIGINT       NOT NULL COMMENT '商家ID',
  `distributor_id`  BIGINT       DEFAULT NULL COMMENT '分销商ID',
  `groupon_id`      BIGINT       NOT NULL COMMENT '所属团购活动ID',
  `name`            VARCHAR(200) NOT NULL COMMENT '团购商品名称',
  `title`           VARCHAR(200) DEFAULT NULL COMMENT '展示标题',
  `content`         TEXT         DEFAULT NULL COMMENT '套餐内容/服务内容',
  `description`     VARCHAR(500) DEFAULT NULL COMMENT '商品说明',
  `cover_image`     VARCHAR(500) DEFAULT NULL COMMENT '封面图URL',
  `detail_images`   TEXT         DEFAULT NULL COMMENT '详情图JSON数组',
  `original_price`  DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '原价，单位元',
  `groupon_price`   DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '团购价/现价，单位元',
  `discount_rate`   DECIMAL(5,2) DEFAULT NULL COMMENT '折扣，如7.5表示7.5折',
  `stock`           INT          NOT NULL DEFAULT 0 COMMENT '团购库存',
  `sales`           INT          NOT NULL DEFAULT 0 COMMENT '团购销量',
  `limit_per_user`  INT          NOT NULL DEFAULT 0 COMMENT '每人限购，0不限',
  `valid_days`      INT          NOT NULL DEFAULT 30 COMMENT '购买后有效天数',
  `store_ids`       VARCHAR(500) DEFAULT NULL COMMENT '可用门店ID JSON数组',
  `status`          INT          NOT NULL DEFAULT 0 COMMENT '状态：0下架 1上架',
  `sort`            INT          NOT NULL DEFAULT 0 COMMENT '排序值，越大越靠前',
  `del_flag`        CHAR(1)      DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_groupon_id` (`groupon_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_distributor_id` (`distributor_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团购商品明细表';

-- 27. 分销商表
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

-- 28. 商家结算记录表
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

-- 29. 订单三方分账流水表
DROP TABLE IF EXISTS `order_profit_ledger`;
CREATE TABLE `order_profit_ledger` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT,
  `order_no`         VARCHAR(64)  NOT NULL COMMENT '订单号',
  `merchant_id`      BIGINT       NOT NULL COMMENT '商家ID',
  `distributor_id`   BIGINT       DEFAULT NULL COMMENT '分销商ID',
  `pay_amount`       DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '用户实付金额，单位元',
  `merchant_amount`  DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '商家应得金额，单位元',
  `platform_amount`  DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '平台抽成金额，单位元',
  `distributor_amount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '分销商佣金金额，单位元',
  `merchant_rate`    DECIMAL(5,2)  DEFAULT NULL COMMENT '商家比例, 如85.00',
  `platform_rate`    DECIMAL(5,2)  DEFAULT NULL COMMENT '平台比例, 如10.00',
  `distributor_rate` DECIMAL(5,2)  DEFAULT NULL COMMENT '分销商比例, 如5.00',
  `status`           VARCHAR(32)  NOT NULL DEFAULT 'WAITING_SETTLEMENT' COMMENT 'WAITING_SETTLEMENT/SETTLED/REFUND_REVERSED',
  `finish_time`      DATETIME     DEFAULT NULL COMMENT '订单完成时间',
  `del_flag`         CHAR(1)      DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_distributor_id` (`distributor_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单三方分账流水表';

-- 30. 分销商佣金结算记录表
DROP TABLE IF EXISTS `distributor_settlement_record`;
CREATE TABLE `distributor_settlement_record` (
  `id`                        BIGINT       NOT NULL AUTO_INCREMENT,
  `settlement_no`             VARCHAR(64)  NOT NULL COMMENT '结算单号',
  `distributor_id`            BIGINT       NOT NULL COMMENT '分销商ID',
  `merchant_id`               BIGINT       DEFAULT NULL COMMENT '关联商家ID',
  `order_no`                  VARCHAR(64)  DEFAULT NULL COMMENT '关联订单号',
  `amount`                    DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '佣金金额，单位元',
  `rate`                      DECIMAL(5,2)  DEFAULT NULL COMMENT '佣金比例',
  `status`                    VARCHAR(32)  NOT NULL DEFAULT 'WAITING_SETTLEMENT' COMMENT 'WAITING_SETTLEMENT/TRANSFERRING/ARRIVED/FAILED/CANCELLED/REVERSED',
  `settlement_period_start`   DATE         DEFAULT NULL COMMENT '结算周期开始',
  `settlement_period_end`     DATE         DEFAULT NULL COMMENT '结算周期结束',
  `expected_transfer_time`    DATETIME     DEFAULT NULL COMMENT '预计打款时间',
  `transfer_time`             DATETIME     DEFAULT NULL COMMENT '发起打款时间',
  `arrive_time`               DATETIME     DEFAULT NULL COMMENT '到账时间',
  `fail_reason`               VARCHAR(500) DEFAULT NULL COMMENT '失败原因',
  `reverse_record_id`         BIGINT       DEFAULT NULL COMMENT '逆向记录ID',
  `del_flag`                  CHAR(1)      DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_time`               DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`               DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_settlement_no` (`settlement_no`),
  KEY `idx_distributor_id` (`distributor_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_status` (`status`),
  KEY `idx_expected_transfer_time` (`expected_transfer_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分销商佣金结算记录表';
