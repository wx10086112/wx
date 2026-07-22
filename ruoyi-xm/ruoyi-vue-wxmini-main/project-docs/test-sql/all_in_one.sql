-- ============================================
-- 零点科技多商家SaaS团购商城 - 一键建表+数据(合并版)
-- 数据库: ruoyi-cs
-- 执行这一个文件即可，会先删除旧表再重建
-- 共30张业务表
-- ============================================

USE `ruoyi-cs`;

-- ============================================================
-- 第一部分: 建表 (24张)
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
  `wx_payment_access_type` VARCHAR(32) DEFAULT 'EXISTING_MCH' COMMENT '微信支付接入方式：EXISTING_MCH已有商户号 APPLYMENT_ASSISTED平台协助申请',
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

-- 7. 商城用户表
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

-- 7. 优惠券模板表
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

-- 8. 用户优惠券表
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

-- 9. 用户收藏表
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

-- 10. 用户收货地址表
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

-- 11. 订单表
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

-- 12. 订单商品明细表
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

-- 13. 资金流水表
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

-- 14. 提现记录表
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

-- 15. 平台收益表
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
  UNIQUE KEY `uk_active_order_no` (`order_no`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台收益表';

-- 16. 商家账单表
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

-- 17. 操作日志表
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

-- 18. 登录日志表(运营后台)
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

-- 19. 操作日志表(运营后台)
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

-- 20. 购物车表
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

-- 21. 首页轮播图/推荐位表
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

-- 22. 支付记录表
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
  `active_order_no` VARCHAR(64) GENERATED ALWAYS AS (CASE WHEN `del_flag` = '0' THEN `order_no` ELSE NULL END) STORED COMMENT '有效订单号唯一约束辅助列',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_record_active_order_no` (`active_order_no`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_transaction_id` (`transaction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- 23. 退款记录表
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
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1待审核 2已通过待微信退款 3已拒绝 4已退款 5退款异常',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `refund_time` DATETIME DEFAULT NULL COMMENT '退款完成时间',
  `reject_reason` VARCHAR(255) DEFAULT '' COMMENT '拒绝原因',
  `operator` VARCHAR(64) DEFAULT '' COMMENT '操作人',
  `retry_count` INT NOT NULL DEFAULT 0 COMMENT '微信退款重试次数',
  `last_retry_time` DATETIME DEFAULT NULL COMMENT '最近一次退款重试时间',
  `next_retry_time` DATETIME DEFAULT NULL COMMENT '下次允许退款重试时间',
  `last_retry_reason` VARCHAR(500) DEFAULT NULL COMMENT '最近一次退款重试失败原因',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_refund_retry` (`status`, `next_retry_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款记录表';

-- 24. 核销记录表
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

-- 25. 团购商品明细表
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
  `dish_groups`     LONGTEXT     DEFAULT NULL COMMENT '菜品组JSON',
  `dish_total_price` BIGINT      DEFAULT 0 COMMENT '菜品总价，单位分',
  `direct_total_price` TINYINT   DEFAULT 0 COMMENT '是否直接设置菜品总价：0否 1是',
  `dish_count`      INT          DEFAULT 0 COMMENT '菜品数量统计',
  `available_dish_count` INT     DEFAULT 0 COMMENT '实际可享用菜品数量',
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

-- 26. 分销商表
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

-- 27. 商家结算记录表
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
  UNIQUE KEY `uk_active_order_no` (`order_no`, `del_flag`),
  KEY `idx_expected_transfer_time` (`expected_transfer_time`),
  KEY `idx_distributor_id` (`distributor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家结算记录表';

-- 28. 订单三方分账流水表
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

-- 29. 分销商佣金结算记录表
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
  UNIQUE KEY `uk_active_order_distributor` (`order_no`, `distributor_id`, `del_flag`),
  KEY `idx_status` (`status`),
  KEY `idx_expected_transfer_time` (`expected_transfer_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分销商佣金结算记录表';

-- ============================================================
-- 第二部分: 测试数据
-- ============================================================

-- 1. 商家（抽成比例直接在商家表上,后台可动态调整）
INSERT INTO `merchant` (`id`, `distributor_id`, `name`, `logo`, `contact`, `phone`, `commission_rate`, `status`, `balance`, `total_income`, `address`, `avatar`, `description`, `business_hours`, `product_count`, `store_count`) VALUES
(1, NULL, '鲜果园水果店', '/profile/upload/merchant/fruit_logo.png', '张三', '13800001111', 5.00, 1, 12580.50, 58320.00, '北京市朝阳区建国路88号', '/profile/upload/merchant/fruit_cover.png', '新鲜水果,产地直供,品质保证', '08:00-22:00', 6, 2),
(2, NULL, '好味烘焙坊', '/profile/upload/merchant/bakery_logo.png', '李四', '13800002222', 8.00, 1, 8960.00, 35600.00, '北京市海淀区中关村大街12号', '/profile/upload/merchant/bakery_cover.png', '手工烘焙,现做现卖,甜蜜每一天', '07:00-21:00', 5, 1),
(3, NULL, '川味小厨', '/profile/upload/merchant/chuan_logo.png', '王五', '13800003333', 12.00, 1, 5200.00, 18900.00, '北京市西城区西单北大街56号', '/profile/upload/merchant/chuan_cover.png', '正宗川菜,麻辣鲜香,地道风味', '10:00-23:00', 5, 1);

-- 2. 商家登录账号（密码统一为 admin123，BCrypt加密）
INSERT INTO `merchant_user` (`id`, `merchant_id`, `username`, `password`, `real_name`, `phone`, `role`, `status`) VALUES
(1, 1, 'fruit_admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '张三', '13800001111', 'owner', 1),
(2, 1, 'fruit_staff', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '赵六', '13900001111', 'member', 1),
(3, 2, 'bakery_admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '李四', '13800002222', 'owner', 1),
(4, 3, 'chuan_admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '王五', '13800003333', 'owner', 1);

-- 3. 门店
INSERT INTO `merchant_store` (`id`, `merchant_id`, `name`, `contact`, `phone`, `address`, `longitude`, `latitude`, `business_hours`, `avatar`, `status`, `is_main`) VALUES
(1, 1, '鲜果园(建国路总店)', '张三', '13800001111', '北京市朝阳区建国路88号', 116.4718750, 39.9150420, '08:00-22:00', '/profile/upload/store/fruit_main.jpg', 1, 1),
(2, 1, '鲜果园(望京分店)', '赵六', '13900001111', '北京市朝阳区望京西路68号', 116.4801230, 39.9904560, '08:00-22:00', '/profile/upload/store/fruit_wj.jpg', 1, 0),
(3, 2, '好味烘焙(中关村店)', '李四', '13800002222', '北京市海淀区中关村大街12号', 116.3168760, 39.9834520, '07:00-21:00', '/profile/upload/store/bakery_zgc.jpg', 1, 1),
(4, 3, '川味小厨(西单店)', '王五', '13800003333', '北京市西城区西单北大街56号', 116.3739870, 39.9123450, '10:00-23:00', '/profile/upload/store/chuan_xd.jpg', 1, 1);

-- 4. 商品分类
INSERT INTO `product_category` (`id`, `merchant_id`, `name`, `sort`) VALUES
(1, 1, '时令水果', 100),
(2, 1, '进口水果', 90),
(3, 1, '水果礼盒', 80),
(4, 2, '面包蛋糕', 100),
(5, 2, '甜品点心', 90),
(6, 2, '饮品', 80),
(7, 3, '招牌菜', 100),
(8, 3, '经典川菜', 90),
(9, 3, '小吃凉菜', 80);

-- 5. 团购活动
INSERT INTO `groupon_activity` (`id`, `merchant_id`, `name`, `cover_image`, `description`, `start_time`, `end_time`, `status`, `total_sold`, `limit_per_user`) VALUES
(1, 1, '春季水果狂欢节', '/profile/upload/groupon/spring_fruit.jpg', '春季时令水果大促,全场8折起,满100减20', '2026-04-01 00:00:00', '2026-06-30 23:59:59', 1, 356, 5),
(2, 2, '烘焙新品尝鲜周', '/profile/upload/groupon/bakery_new.jpg', '新品面包蛋糕限时特惠,第二件半价', '2026-05-01 00:00:00', '2026-05-31 23:59:59', 1, 128, 3),
(3, 3, '川味美食节', '/profile/upload/groupon/chuan_fest.jpg', '地道川菜团购特惠,套餐低至6折', '2026-05-10 00:00:00', '2026-06-10 23:59:59', 1, 89, 0);

-- 6. 商品
INSERT INTO `product` (`id`, `merchant_id`, `category_id`, `groupon_id`, `name`, `cover_image`, `images`, `price`, `original_price`, `stock`, `sales`, `status`, `valid_days`, `description`, `store_ids`, `sort`) VALUES
(1, 1, 1, 1, '精选红富士苹果5斤装', '/profile/upload/product/apple_cover.jpg', '[\"/profile/upload/product/apple_1.jpg\",\"/profile/upload/product/apple_2.jpg\",\"/profile/upload/product/apple_3.jpg\"]', 29.90, 49.90, 200, 86, 1, 7, '山东烟台红富士,脆甜多汁,5斤装约12-15个', '[1,2]', 100),
(2, 1, 1, 1, '海南金煌芒果3斤装', '/profile/upload/product/mango_cover.jpg', '[\"/profile/upload/product/mango_1.jpg\",\"/profile/upload/product/mango_2.jpg\"]', 39.90, 69.90, 150, 62, 1, 7, '海南直采金煌芒果,个大核薄,香甜可口', '[1,2]', 90),
(3, 1, 2, 1, '智利进口车厘子1斤', '/profile/upload/product/cherry_cover.jpg', '[\"/profile/upload/product/cherry_1.jpg\",\"/profile/upload/product/cherry_2.jpg\"]', 59.90, 89.90, 80, 45, 1, 3, '智利JJ级车厘子,果径28mm+,脆甜爽口', '[1]', 80),
(4, 1, 3, NULL, '精品水果礼盒(大)', '/profile/upload/product/box_cover.jpg', '[\"/profile/upload/product/box_1.jpg\"]', 128.00, 198.00, 50, 23, 1, 7, '精选8种时令水果,精美包装,送礼佳选', '[1,2]', 70),
(5, 1, 1, NULL, '广西百香果10个装', '/profile/upload/product/passion_cover.jpg', '[\"/profile/upload/product/passion_1.jpg\"]', 15.90, 25.90, 300, 128, 1, 10, '广西北流百香果,酸甜可口,泡水直饮两相宜', '[1,2]', 60),
(6, 1, 2, NULL, '泰国山竹5斤装', '/profile/upload/product/mangosteen_cover.jpg', '[\"/profile/upload/product/mangosteen_1.jpg\"]', 89.00, 139.00, 60, 15, 0, 5, '泰国进口山竹,果肉洁白,清甜多汁', '[1]', 50),
(7, 2, 4, 2, '招牌奶油草莓蛋糕(6寸)', '/profile/upload/product/cake_cover.jpg', '[\"/profile/upload/product/cake_1.jpg\",\"/profile/upload/product/cake_2.jpg\"]', 68.00, 98.00, 30, 42, 1, 1, '新鲜草莓+动物奶油,当日现做', '[3]', 100),
(8, 2, 4, 2, '全麦核桃吐司', '/profile/upload/product/bread_cover.jpg', '[\"/profile/upload/product/bread_1.jpg\"]', 18.00, 28.00, 50, 88, 1, 3, '全麦面粉+新疆核桃,健康早餐首选', '[3]', 90),
(9, 2, 5, 2, '手工蛋黄酥6个装', '/profile/upload/product/egg_cover.jpg', '[\"/profile/upload/product/egg_1.jpg\",\"/profile/upload/product/egg_2.jpg\"]', 32.00, 48.00, 100, 56, 1, 5, '酥皮+咸蛋黄+红豆沙,一口三重口感', '[3]', 80),
(10, 2, 6, NULL, '现磨拿铁咖啡(大杯)', '/profile/upload/product/coffee_cover.jpg', '[\"/profile/upload/product/coffee_1.jpg\"]', 22.00, 32.00, 999, 167, 1, 1, '阿拉比卡咖啡豆现磨,鲜牛奶打发', '[3]', 70),
(11, 2, 5, NULL, '蔓越莓曲奇饼干礼盒', '/profile/upload/product/cookie_cover.jpg', '[\"/profile/upload/product/cookie_1.jpg\"]', 45.00, 68.00, 80, 34, 1, 30, '进口黄油+蔓越莓干,酥脆香甜,送礼佳品', '[3]', 60),
(12, 3, 7, 3, '招牌水煮鱼套餐', '/profile/upload/product/fish_cover.jpg', '[\"/profile/upload/product/fish_1.jpg\",\"/profile/upload/product/fish_2.jpg\"]', 68.00, 108.00, 50, 38, 1, 1, '鲜活草鱼,麻辣鲜香,含米饭+小菜', '[4]', 100),
(13, 3, 7, 3, '麻婆豆腐套餐', '/profile/upload/product/tofu_cover.jpg', '[\"/profile/upload/product/tofu_1.jpg\"]', 28.00, 42.00, 999, 95, 1, 1, '正宗四川麻婆豆腐,麻辣烫鲜嫩,含米饭', '[4]', 90),
(14, 3, 8, 3, '回锅肉套餐', '/profile/upload/product/pork_cover.jpg', '[\"/profile/upload/product/pork_1.jpg\"]', 38.00, 58.00, 200, 67, 1, 1, '蒜苗回锅肉,肥而不腻,含米饭+小菜', '[4]', 80),
(15, 3, 9, NULL, '夫妻肺片', '/profile/upload/product/lung_cover.jpg', '[\"/profile/upload/product/lung_1.jpg\"]', 32.00, 48.00, 150, 43, 1, 1, '红油夫妻肺片,牛肉牛肚,麻辣鲜香', '[4]', 70),
(16, 3, 9, NULL, '酸辣凉粉', '/profile/upload/product/jelly_cover.jpg', '[\"/profile/upload/product/jelly_1.jpg\"]', 12.00, 18.00, 300, 112, 1, 1, '手工豌豆凉粉,酸辣爽口,夏日必备', '[4]', 60);

-- 7. 商城用户
INSERT INTO `mall_user` (`id`, `nickname`, `phone`, `avatar`, `gender`, `city`, `open_id`, `status`, `total_orders`, `total_amount`) VALUES
(1, '小明', '15000001111', 'https://thirdwx.qlogo.cn/mmopen/vi_32/abc1', 1, '北京', 'oWxUser001abc', 1, 5, 326.70),
(2, '爱吃水果的喵', '15000002222', 'https://thirdwx.qlogo.cn/mmopen/vi_32/abc2', 2, '北京', 'oWxUser002def', 1, 3, 189.80),
(3, '美食家老王', '15000003333', 'https://thirdwx.qlogo.cn/mmopen/vi_32/abc3', 1, '上海', 'oWxUser003ghi', 1, 8, 568.50),
(4, '甜品控小李', '15000004444', 'https://thirdwx.qlogo.cn/mmopen/vi_32/abc4', 2, '广州', 'oWxUser004jkl', 1, 2, 136.00),
(5, '打工人小张', '15000005555', 'https://thirdwx.qlogo.cn/mmopen/vi_32/abc5', 1, '深圳', 'oWxUser005mno', 0, 0, 0.00);

-- 8. 优惠券模板
INSERT INTO `coupon` (`id`, `merchant_id`, `name`, `type`, `discount_value`, `min_amount`, `total_count`, `used_count`, `start_time`, `end_time`, `status`) VALUES
(1, 1, '鲜果园新客满50减10', 1, 10.00, 50.00, 500, 186, '2026-04-01 00:00:00', '2026-06-30 23:59:59', 1),
(2, 1, '鲜果园满100减25', 1, 25.00, 100.00, 200, 78, '2026-05-01 00:00:00', '2026-05-31 23:59:59', 1),
(3, 2, '烘焙8折券', 2, 8.00, 30.00, 300, 112, '2026-05-01 00:00:00', '2026-05-31 23:59:59', 1),
(4, 3, '川味小厨满80减15', 1, 15.00, 80.00, 400, 95, '2026-05-10 00:00:00', '2026-06-10 23:59:59', 1),
(5, NULL, '平台通用5元代金券', 3, 5.00, 0.00, 1000, 320, '2026-05-01 00:00:00', '2026-07-31 23:59:59', 1);

-- 9. 用户优惠券
INSERT INTO `user_coupon` (`id`, `user_id`, `coupon_id`, `merchant_id`, `status`, `use_time`, `order_no`, `create_time`) VALUES
(1, 1, 1, 1, 1, '2026-05-08 14:30:00', 'ORD20260508001', '2026-05-01 10:00:00'),
(2, 1, 5, NULL, 0, NULL, '', '2026-05-01 10:00:00'),
(3, 2, 1, 1, 0, NULL, '', '2026-05-02 15:20:00'),
(4, 2, 3, 2, 1, '2026-05-09 11:00:00', 'ORD20260509001', '2026-05-03 09:00:00'),
(5, 3, 2, 1, 0, NULL, '', '2026-05-05 08:30:00'),
(6, 3, 4, 3, 1, '2026-05-10 19:30:00', 'ORD20260510001', '2026-05-10 12:00:00'),
(7, 3, 5, NULL, 0, NULL, '', '2026-05-05 08:30:00'),
(8, 4, 3, 2, 0, NULL, '', '2026-05-06 16:40:00'),
(9, 4, 5, NULL, 2, NULL, '', '2026-04-01 10:00:00'),
(10, 1, 2, 1, 2, NULL, '', '2026-04-05 12:00:00');

-- 10. 用户收藏
INSERT INTO `user_favorite` (`id`, `user_id`, `target_type`, `target_id`, `create_time`) VALUES
(1, 1, 1, 1, '2026-05-01 10:30:00'),
(2, 1, 1, 7, '2026-05-02 14:20:00'),
(3, 1, 2, 1, '2026-05-01 10:32:00'),
(4, 2, 1, 3, '2026-05-03 09:15:00'),
(5, 2, 3, 1, '2026-05-03 09:20:00'),
(6, 3, 1, 12, '2026-05-05 12:00:00'),
(7, 3, 2, 3, '2026-05-05 12:05:00'),
(8, 3, 3, 3, '2026-05-10 11:00:00'),
(9, 4, 1, 9, '2026-05-06 17:00:00'),
(10, 4, 1, 11, '2026-05-06 17:05:00');

-- 11. 用户收货地址
INSERT INTO `user_address` (`id`, `user_id`, `name`, `phone`, `province`, `city`, `district`, `detail`, `is_default`) VALUES
(1, 1, '小明', '15000001111', '北京市', '北京市', '朝阳区', '建国路88号国贸大厦A座1201', 1),
(2, 1, '小明', '15000001111', '北京市', '北京市', '海淀区', '中关村大街1号理想大厦502', 0),
(3, 2, '喵喵', '15000002222', '北京市', '北京市', '朝阳区', '望京西路68号望京SOHO B座808', 1),
(4, 3, '老王', '15000003333', '上海市', '上海市', '浦东新区', '陆家嘴环路1000号恒生大厦18F', 1),
(5, 4, '小李', '15000004444', '广东省', '广州市', '天河区', '天河路385号太古汇商场L3', 1);

-- 12. 订单
INSERT INTO `mall_order` (`id`, `order_no`, `merchant_id`, `user_id`, `store_id`, `total_amount`, `pay_amount`, `commission`, `merchant_income`, `coupon_id`, `coupon_amount`, `groupon_id`, `status`, `write_off_code`, `pay_time`, `use_time`, `complete_time`, `cancel_time`, `refund_time`) VALUES
(1, 'ORD20260508001', 1, 1, 1, 69.80, 59.80, 3.00, 56.80, 1, 10.00, 1, 3, 'LY20260508A7K9M2QX', '2026-05-08 14:35:00', '2026-05-09 10:20:00', '2026-05-09 10:20:00', NULL, NULL),
(2, 'ORD20260509002', 2, 1, 3, 68.00, 68.00, 5.44, 62.56, NULL, 0.00, 2, 1, 'LY20260509D4R8W7KM', '2026-05-09 16:00:00', NULL, NULL, NULL, NULL),
(3, 'ORD20260510003', 1, 1, NULL, 128.00, 128.00, 6.40, 121.60, NULL, 0.00, NULL, 0, '', NULL, NULL, NULL, NULL, NULL),
(4, 'ORD20260509001', 2, 2, 3, 50.00, 42.00, 3.36, 38.64, 3, 8.00, 2, 3, 'LY20260509B8N4T6RP', '2026-05-09 11:05:00', '2026-05-09 15:30:00', '2026-05-09 15:30:00', NULL, NULL),
(5, 'ORD20260510004', 1, 2, 1, 55.80, 55.80, 2.79, 53.01, NULL, 0.00, 1, 1, 'LY20260510C6X9P4HJ', '2026-05-10 09:30:00', NULL, NULL, NULL, NULL),
(6, 'ORD20260510001', 3, 3, 4, 108.00, 93.00, 11.16, 81.84, 4, 15.00, 3, 2, 'LY20260510F7M2K8QW', '2026-05-10 19:35:00', '2026-05-10 20:10:00', NULL, NULL, NULL),
(7, 'ORD20260507005', 1, 3, 2, 89.80, 89.80, 4.49, 85.31, NULL, 0.00, 1, 3, 'LY20260507H3L7V9RT', '2026-05-07 11:00:00', '2026-05-07 16:00:00', '2026-05-07 16:00:00', NULL, NULL),
(8, 'ORD20260506006', 3, 3, NULL, 44.00, 44.00, 5.28, 38.72, NULL, 0.00, NULL, 5, '', NULL, NULL, NULL, '2026-05-06 12:30:00', NULL),
(9, 'ORD20260506007', 2, 4, 3, 113.00, 113.00, 9.04, 103.96, NULL, 0.00, 2, 3, 'LY20260506J4P6X8MN', '2026-05-06 17:30:00', '2026-05-06 18:00:00', '2026-05-06 18:00:00', NULL, NULL),
(10, 'ORD20260504008', 2, 4, NULL, 45.00, 45.00, 3.60, 41.40, NULL, 0.00, NULL, 4, 'LY20260504K8R3W7QH', '2026-05-04 14:00:00', NULL, NULL, NULL, '2026-05-05 10:00:00');

-- 13. 订单商品明细
INSERT INTO `order_item` (`id`, `order_id`, `order_no`, `merchant_id`, `product_id`, `product_name`, `product_image`, `price`, `quantity`, `subtotal`) VALUES
(1, 1, 'ORD20260508001', 1, 1, '精选红富士苹果5斤装', '/profile/upload/product/apple_cover.jpg', 29.90, 1, 29.90),
(2, 1, 'ORD20260508001', 1, 2, '海南金煌芒果3斤装', '/profile/upload/product/mango_cover.jpg', 39.90, 1, 39.90),
(3, 2, 'ORD20260509002', 2, 7, '招牌奶油草莓蛋糕(6寸)', '/profile/upload/product/cake_cover.jpg', 68.00, 1, 68.00),
(4, 3, 'ORD20260510003', 1, 4, '精品水果礼盒(大)', '/profile/upload/product/box_cover.jpg', 128.00, 1, 128.00),
(5, 4, 'ORD20260509001', 2, 9, '手工蛋黄酥6个装', '/profile/upload/product/egg_cover.jpg', 32.00, 1, 32.00),
(6, 4, 'ORD20260509001', 2, 8, '全麦核桃吐司', '/profile/upload/product/bread_cover.jpg', 18.00, 1, 18.00),
(7, 5, 'ORD20260510004', 1, 1, '精选红富士苹果5斤装', '/profile/upload/product/apple_cover.jpg', 29.90, 1, 29.90),
(8, 5, 'ORD20260510004', 1, 5, '广西百香果10个装', '/profile/upload/product/passion_cover.jpg', 15.90, 1, 15.90),
(9, 6, 'ORD20260510001', 3, 12, '招牌水煮鱼套餐', '/profile/upload/product/fish_cover.jpg', 68.00, 1, 68.00),
(10, 6, 'ORD20260510001', 3, 14, '回锅肉套餐', '/profile/upload/product/pork_cover.jpg', 38.00, 1, 38.00),
(11, 7, 'ORD20260507005', 1, 3, '智利进口车厘子1斤', '/profile/upload/product/cherry_cover.jpg', 59.90, 1, 59.90),
(12, 7, 'ORD20260507005', 1, 2, '海南金煌芒果3斤装', '/profile/upload/product/mango_cover.jpg', 29.90, 1, 29.90),
(13, 8, 'ORD20260506006', 3, 15, '夫妻肺片', '/profile/upload/product/lung_cover.jpg', 32.00, 1, 32.00),
(14, 8, 'ORD20260506006', 3, 13, '麻婆豆腐套餐', '/profile/upload/product/tofu_cover.jpg', 12.00, 1, 12.00),
(15, 9, 'ORD20260506007', 2, 7, '招牌奶油草莓蛋糕(6寸)', '/profile/upload/product/cake_cover.jpg', 68.00, 1, 68.00),
(16, 9, 'ORD20260506007', 2, 9, '手工蛋黄酥6个装', '/profile/upload/product/egg_cover.jpg', 32.00, 1, 32.00),
(17, 9, 'ORD20260506007', 2, 10, '现磨拿铁咖啡(大杯)', '/profile/upload/product/coffee_cover.jpg', 22.00, 1, 22.00),
(18, 10, 'ORD20260504008', 2, 11, '蔓越莓曲奇饼干礼盒', '/profile/upload/product/cookie_cover.jpg', 45.00, 1, 45.00);

-- 14. 购物车
INSERT INTO `cart` (`id`, `user_id`, `product_id`, `merchant_id`, `quantity`, `checked`) VALUES
(1, 1, 1, 1, 2, 1),
(2, 1, 3, 1, 1, 1),
(3, 1, 7, 2, 1, 0),
(4, 2, 1, 1, 1, 1),
(5, 2, 2, 1, 1, 1),
(6, 3, 12, 3, 1, 1),
(7, 3, 13, 3, 2, 1),
(8, 3, 1, 1, 1, 0),
(9, 4, 7, 2, 1, 1),
(10, 4, 9, 2, 3, 1);

-- 15. 首页轮播图
INSERT INTO `banner` (`id`, `title`, `image`, `link_type`, `link_id`, `link_url`, `sort`, `status`, `position`) VALUES
(1, '春季水果狂欢节', '/profile/upload/banner/spring_fruit.jpg', 2, 1, '', 100, 1, 'home'),
(2, '新品烘焙尝鲜', '/profile/upload/banner/bakery_new.jpg', 2, 2, '', 90, 1, 'home'),
(3, '川味美食节开吃啦', '/profile/upload/banner/chuan_fest.jpg', 2, 3, '', 80, 1, 'home'),
(4, '车厘子限时特价', '/profile/upload/banner/cherry_sale.jpg', 1, 3, '', 70, 1, 'home'),
(5, '优惠券大放送', '/profile/upload/banner/coupon_banner.jpg', 0, NULL, '', 60, 1, 'home'),
(6, '5元无门槛券', '/profile/upload/banner/coupon_5yuan.jpg', 0, NULL, '', 100, 1, 'coupon');

-- 16. 支付记录
INSERT INTO `payment_record` (`id`, `order_no`, `merchant_id`, `user_id`, `amount`, `pay_type`, `transaction_id`, `out_trade_no`, `pay_status`, `pay_time`, `notify_result`) VALUES
(1, 'ORD20260508001', 1, 1, 59.80, 'wechat', 'WX20260508143500001', 'MCH_ORD20260508001', 1, '2026-05-08 14:35:00', 'success'),
(2, 'ORD20260509002', 2, 1, 68.00, 'wechat', 'WX20260509160000002', 'MCH_ORD20260509002', 1, '2026-05-09 16:00:00', 'success'),
(3, 'ORD20260509001', 2, 2, 42.00, 'wechat', 'WX20260509110500003', 'MCH_ORD20260509001', 1, '2026-05-09 11:05:00', 'success'),
(4, 'ORD20260510004', 1, 2, 55.80, 'wechat', 'WX20260510093000004', 'MCH_ORD20260510004', 1, '2026-05-10 09:30:00', 'success'),
(5, 'ORD20260510001', 3, 3, 93.00, 'wechat', 'WX20260510193500005', 'MCH_ORD20260510001', 1, '2026-05-10 19:35:00', 'success'),
(6, 'ORD20260507005', 1, 3, 89.80, 'wechat', 'WX20260507110000006', 'MCH_ORD20260507005', 1, '2026-05-07 11:00:00', 'success'),
(7, 'ORD20260506007', 2, 4, 113.00, 'wechat', 'WX20260506173000007', 'MCH_ORD20260506007', 1, '2026-05-06 17:30:00', 'success'),
(8, 'ORD20260504008', 2, 4, 45.00, 'wechat', 'WX20260504140000008', 'MCH_ORD20260504008', 3, '2026-05-04 14:00:00', 'success');

-- 17. 资金流水
INSERT INTO `transaction_record` (`id`, `merchant_id`, `type`, `amount`, `balance`, `order_no`, `description`) VALUES
(NULL, NULL, 'payment', -59.80, NULL, 'ORD20260508001', '用户支付订单ORD20260508001'),
(NULL, NULL, 'payment', -68.00, NULL, 'ORD20260509002', '用户支付订单ORD20260509002'),
(NULL, NULL, 'payment', -42.00, NULL, 'ORD20260509001', '用户支付订单ORD20260509001'),
(NULL, NULL, 'payment', -55.80, NULL, 'ORD20260510004', '用户支付订单ORD20260510004'),
(NULL, NULL, 'payment', -93.00, NULL, 'ORD20260510001', '用户支付订单ORD20260510001'),
(NULL, NULL, 'payment', -89.80, NULL, 'ORD20260507005', '用户支付订单ORD20260507005'),
(NULL, NULL, 'payment', -113.00, NULL, 'ORD20260506007', '用户支付订单ORD20260506007'),
(NULL, 1, 'income', 56.80, 12580.50, 'ORD20260508001', '商家收入-订单ORD20260508001'),
(NULL, 2, 'income', 62.56, 8960.00, 'ORD20260509002', '商家收入-订单ORD20260509002'),
(NULL, 2, 'income', 38.64, 8960.00, 'ORD20260509001', '商家收入-订单ORD20260509001'),
(NULL, 1, 'income', 53.01, 12580.50, 'ORD20260510004', '商家收入-订单ORD20260510004'),
(NULL, 3, 'income', 81.84, 5200.00, 'ORD20260510001', '商家收入-订单ORD20260510001'),
(NULL, 1, 'income', 85.31, 12580.50, 'ORD20260507005', '商家收入-订单ORD20260507005'),
(NULL, 2, 'income', 103.96, 8960.00, 'ORD20260506007', '商家收入-订单ORD20260506007'),
(NULL, 2, 'refund', -45.00, 8960.00, 'ORD20260504008', '退款-订单ORD20260504008');

-- 18. 退款记录
INSERT INTO `refund_record` (`id`, `order_no`, `refund_no`, `merchant_id`, `user_id`, `payment_record_id`, `refund_amount`, `refund_reason`, `refund_type`, `status`, `audit_time`, `refund_time`, `reject_reason`, `operator`) VALUES
(1, 'ORD20260504008', 'RF20260505001', 2, 4, 8, 45.00, '商品与描述不符,口味不满意', 1, 3, '2026-05-05 09:30:00', '2026-05-05 10:00:00', '', 'admin');

-- 19. 平台收益
INSERT INTO `platform_income` (`id`, `merchant_id`, `order_no`, `order_amount`, `commission_rate`, `commission`) VALUES
(1, 1, 'ORD20260508001', 59.80, 5.00, 3.00),
(2, 2, 'ORD20260509002', 68.00, 8.00, 5.44),
(3, 2, 'ORD20260509001', 42.00, 8.00, 3.36),
(4, 1, 'ORD20260510004', 55.80, 5.00, 2.79),
(5, 3, 'ORD20260510001', 93.00, 12.00, 11.16),
(6, 1, 'ORD20260507005', 89.80, 5.00, 4.49),
(7, 2, 'ORD20260506007', 113.00, 8.00, 9.04);

-- 20. 提现记录
INSERT INTO `withdraw_record` (`id`, `merchant_id`, `amount`, `bank_name`, `bank_account`, `account_name`, `status`, `audit_time`, `pay_time`, `reject_reason`) VALUES
(1, 1, 5000.00, '中国工商银行', '6222021234567890123', '张三', 2, '2026-05-05 10:00:00', '2026-05-05 14:00:00', ''),
(2, 2, 3000.00, '中国建设银行', '6227001234567890456', '李四', 2, '2026-05-06 09:00:00', '2026-05-06 15:00:00', ''),
(3, 3, 2000.00, '中国银行', '6217001234567890789', '王五', 1, '2026-05-10 10:00:00', NULL, ''),
(4, 1, 2000.00, '中国工商银行', '6222021234567890123', '张三', 0, NULL, NULL, '');

-- 21. 商家账单
INSERT INTO `merchant_bill` (`id`, `merchant_id`, `bill_no`, `bill_type`, `start_date`, `end_date`, `total_orders`, `total_amount`, `total_commission`, `net_income`, `status`, `settle_time`) VALUES
(1, 1, 'BILL20260501M01', 'monthly', '2026-05-01', '2026-05-31', 3, 277.60, 13.88, 263.72, 0, NULL),
(2, 2, 'BILL20260501M02', 'monthly', '2026-05-01', '2026-05-31', 3, 223.00, 17.84, 205.16, 0, NULL),
(3, 3, 'BILL20260501M03', 'monthly', '2026-05-01', '2026-05-31', 1, 93.00, 11.16, 81.84, 0, NULL);

-- 22. 操作日志(通用)
INSERT INTO `operation_log` (`id`, `operator`, `operator_type`, `module`, `action`, `method`, `ip`, `status`, `cost_time`, `oper_time`) VALUES
(1, 'admin', 'admin', '商家管理', '审核商家', 'com.ruoyi.merchant.controller.MerchantController.audit()', '192.168.1.100', 0, 120, '2026-05-01 09:00:00'),
(2, 'admin', 'admin', '订单管理', '查询订单列表', 'com.ruoyi.order.controller.OrderController.list()', '192.168.1.100', 0, 85, '2026-05-10 10:30:00'),
(3, 'fruit_admin', 'merchant', '商品管理', '上架商品', 'com.ruoyi.merchant.controller.ProductController.onShelf()', '192.168.1.101', 0, 95, '2026-05-02 14:00:00'),
(4, 'bakery_admin', 'merchant', '订单管理', '核销订单', 'com.ruoyi.merchant.controller.OrderController.writeOff()', '192.168.1.102', 0, 110, '2026-05-09 15:30:00'),
(5, 'admin', 'admin', '财务管理', '审核提现', 'com.ruoyi.finance.controller.WithdrawController.audit()', '192.168.1.100', 0, 200, '2026-05-06 09:00:00');

-- 23. 登录日志(运营后台)
INSERT INTO `mall_login_log` (`id`, `user_name`, `ip`, `location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES
(1, 'admin', '192.168.1.100', '北京市', 'Chrome 120', 'Windows 11', 0, '登录成功', '2026-05-10 08:30:00'),
(2, 'admin', '192.168.1.100', '北京市', 'Chrome 120', 'Windows 11', 0, '登录成功', '2026-05-09 08:45:00'),
(3, 'ry', '192.168.1.105', '上海市', 'Edge 120', 'Windows 10', 0, '登录成功', '2026-05-10 09:00:00'),
(4, 'admin', '10.0.0.1', '未知', 'Firefox 115', 'Mac OS', 1, '密码错误', '2026-05-10 03:15:00'),
(5, 'admin', '192.168.1.100', '北京市', 'Chrome 120', 'Windows 11', 0, '登录成功', '2026-05-08 09:10:00');

-- 24. 操作日志(运营后台)
INSERT INTO `mall_oper_log` (`id`, `operator`, `module`, `operation`, `method`, `ip`, `status`, `cost_time`, `oper_time`) VALUES
(1, 'admin', '商家管理', '审核通过商家"川味小厨"', 'com.ruoyi.merchant.controller.MerchantController.audit()', '192.168.1.100', 0, 150, '2026-05-01 09:00:00'),
(2, 'admin', '订单管理', '导出订单报表', 'com.ruoyi.order.controller.OrderController.export()', '192.168.1.100', 0, 2300, '2026-05-10 10:30:00'),
(3, 'admin', '财务管理', '通过提现申请#3', 'com.ruoyi.finance.controller.WithdrawController.approve()', '192.168.1.100', 0, 180, '2026-05-10 10:00:00'),
(4, 'ry', '轮播图管理', '新增轮播图"川味美食节"', 'com.ruoyi.content.controller.BannerController.add()', '192.168.1.105', 0, 95, '2026-05-10 09:30:00'),
(5, 'admin', '优惠券管理', '新增优惠券"平台通用5元代金券"', 'com.ruoyi.coupon.controller.CouponController.add()', '192.168.1.100', 0, 110, '2026-05-01 10:00:00');
