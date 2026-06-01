-- 2026-06-01 商户管理、腾讯地图、微信支付接入与三方分账字段
-- 目标表：merchant
-- 最新口径：
-- 1. 后台新增商户默认正常(status=1)，无需入驻审核。
-- 2. status=3 表示停止合作，保留商户、商品、订单、分销商归属。
-- 3. 腾讯地图 POI/认领为门店资料字段，第一阶段不强制影响上架。
-- 4. 微信支付兼容两种商户：已有微信支付商户号、平台协助申请。
-- 5. 分账页面展示商家/平台/分销商三方比例，合计必须为100%。
-- 6. 项目未上线，不保留旧兼容字段。

ALTER TABLE `merchant`
  MODIFY COLUMN `status` TINYINT DEFAULT 1 COMMENT '状态(0禁用 1正常 2待审核历史兼容 3停止合作)';

ALTER TABLE `merchant`
  ADD COLUMN `map_claim_status` VARCHAR(32) DEFAULT 'NOT_CLAIMED' COMMENT '腾讯地图认领状态：NOT_CLAIMED未认领 CLAIMING认领中 CLAIMED已认领 FAILED认领失败' AFTER `receiver_type`,
  ADD COLUMN `map_poi_id` VARCHAR(128) DEFAULT NULL COMMENT '腾讯地图POI ID' AFTER `map_claim_status`,
  ADD COLUMN `map_claim_url` VARCHAR(500) DEFAULT NULL COMMENT '腾讯地图认领或门店链接' AFTER `map_poi_id`,
  ADD COLUMN `map_claim_time` DATETIME DEFAULT NULL COMMENT '腾讯地图认领完成时间' AFTER `map_claim_url`,
  ADD COLUMN `map_claim_remark` VARCHAR(500) DEFAULT NULL COMMENT '腾讯地图认领备注' AFTER `map_claim_time`;

-- 可选协助申请字段：只有“平台协助商家申请微信支付商户号”时使用。
ALTER TABLE `merchant`
  ADD COLUMN `wx_payment_access_type` VARCHAR(32) DEFAULT 'EXISTING_MCH' COMMENT '微信支付接入方式：EXISTING_MCH已有商户号 APPLYMENT_ASSISTED平台协助申请' AFTER `map_claim_remark`,
  ADD COLUMN `wx_applyment_id` VARCHAR(128) DEFAULT NULL COMMENT '微信支付进件申请单号，仅平台协助申请时使用' AFTER `wx_payment_access_type`,
  ADD COLUMN `wx_applyment_state` VARCHAR(64) DEFAULT 'NOT_SUBMITTED' COMMENT '微信进件状态，仅平台协助申请时使用' AFTER `wx_applyment_id`,
  ADD COLUMN `wx_applyment_reject_reason` VARCHAR(1000) DEFAULT NULL COMMENT '微信进件驳回原因，仅平台协助申请时使用' AFTER `wx_applyment_state`,
  ADD COLUMN `wx_applyment_time` DATETIME DEFAULT NULL COMMENT '微信进件提交时间，仅平台协助申请时使用' AFTER `wx_applyment_reject_reason`,
  ADD COLUMN `wx_applyment_finish_time` DATETIME DEFAULT NULL COMMENT '微信进件完成时间，仅平台协助申请时使用' AFTER `wx_applyment_time`;

-- 商家已有或申请通过后的微信支付账号。
ALTER TABLE `merchant`
  ADD COLUMN `merchant_wx_mch_id` VARCHAR(64) DEFAULT NULL COMMENT '商家自己的微信支付商户号' AFTER `wx_applyment_finish_time`,
  ADD COLUMN `merchant_wx_mch_name` VARCHAR(200) DEFAULT NULL COMMENT '商家微信支付商户名称' AFTER `merchant_wx_mch_id`;

-- 微信分账配置：页面必须看到商家、平台、分销商三方比例。
ALTER TABLE `merchant`
  ADD COLUMN `wx_profit_sharing_enabled` TINYINT DEFAULT 0 COMMENT '是否启用微信分账：0否 1是' AFTER `merchant_wx_mch_name`,
  ADD COLUMN `platform_receiver_mch_id` VARCHAR(64) DEFAULT NULL COMMENT '平台作为分账接收方的微信商户号' AFTER `wx_profit_sharing_enabled`,
  ADD COLUMN `distributor_receiver_mch_id` VARCHAR(64) DEFAULT NULL COMMENT '分销商作为分账接收方的微信商户号' AFTER `platform_receiver_mch_id`,
  ADD COLUMN `merchant_share_rate` DECIMAL(5,2) DEFAULT 100.00 COMMENT '商家留存比例，单位百分比' AFTER `distributor_receiver_mch_id`,
  ADD COLUMN `platform_share_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT '平台分账比例，单位百分比' AFTER `merchant_share_rate`,
  ADD COLUMN `distributor_share_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT '分销商分账比例，单位百分比' AFTER `platform_share_rate`,
  ADD COLUMN `settlement_cycle` VARCHAR(16) DEFAULT 'T1' COMMENT '到账周期，默认T1' AFTER `distributor_share_rate`;

CREATE INDEX `idx_merchant_map_claim_status` ON `merchant` (`map_claim_status`);
CREATE INDEX `idx_merchant_payment_access_type` ON `merchant` (`wx_payment_access_type`);
CREATE INDEX `idx_merchant_wx_mch_id` ON `merchant` (`merchant_wx_mch_id`);
CREATE INDEX `idx_merchant_profit_sharing_enabled` ON `merchant` (`wx_profit_sharing_enabled`);
