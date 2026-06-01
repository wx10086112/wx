-- 2026-06-01 商户微信支付接入与三方分账字段增量脚本

ALTER TABLE `merchant`
  ADD COLUMN `wx_payment_access_type` VARCHAR(32) DEFAULT 'EXISTING_MCH' COMMENT '微信支付接入方式：EXISTING_MCH已有商户号 APPLYMENT_ASSISTED平台协助申请' AFTER `wx_applyment_finish_time`,
  ADD COLUMN `merchant_wx_mch_id` VARCHAR(64) DEFAULT NULL COMMENT '商家自己的微信支付商户号' AFTER `wx_payment_access_type`,
  ADD COLUMN `merchant_wx_mch_name` VARCHAR(200) DEFAULT NULL COMMENT '商家微信支付商户名称' AFTER `merchant_wx_mch_id`,
  ADD COLUMN `wx_profit_sharing_enabled` TINYINT DEFAULT 0 COMMENT '是否启用微信分账：0否 1是' AFTER `merchant_wx_mch_name`,
  ADD COLUMN `platform_receiver_mch_id` VARCHAR(64) DEFAULT NULL COMMENT '平台作为分账接收方的微信商户号' AFTER `wx_profit_sharing_enabled`,
  ADD COLUMN `distributor_receiver_mch_id` VARCHAR(64) DEFAULT NULL COMMENT '分销商作为分账接收方的微信商户号' AFTER `platform_receiver_mch_id`,
  ADD COLUMN `merchant_share_rate` DECIMAL(5,2) DEFAULT 100.00 COMMENT '商家留存比例，单位百分比' AFTER `distributor_receiver_mch_id`,
  ADD COLUMN `platform_share_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT '平台分账比例，单位百分比' AFTER `merchant_share_rate`,
  ADD COLUMN `distributor_share_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT '分销商分账比例，单位百分比' AFTER `platform_share_rate`,
  ADD COLUMN `settlement_cycle` VARCHAR(16) DEFAULT 'T1' COMMENT '到账周期，默认T1' AFTER `distributor_share_rate`;

CREATE INDEX `idx_merchant_payment_access_type` ON `merchant` (`wx_payment_access_type`);
CREATE INDEX `idx_merchant_wx_mch_id` ON `merchant` (`merchant_wx_mch_id`);
CREATE INDEX `idx_merchant_profit_sharing_enabled` ON `merchant` (`wx_profit_sharing_enabled`);
