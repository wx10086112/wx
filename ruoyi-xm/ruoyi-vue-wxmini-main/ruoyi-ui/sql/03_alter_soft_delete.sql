-- ============================================
-- 软删除支持：为 mall 业务表添加 del_flag 列
-- 执行顺序: 在 01 + 02 之后执行
-- ============================================

USE `ruoyi-cs`;

-- mall_user
ALTER TABLE `mall_user` ADD COLUMN `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志(0存在 2删除)' AFTER `remark`;
UPDATE `mall_user` SET del_flag = '0' WHERE del_flag IS NULL;

-- user_info
ALTER TABLE `user_info` ADD COLUMN `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志(0存在 2删除)' AFTER `update_time`;
UPDATE `user_info` SET del_flag = '0' WHERE del_flag IS NULL;

-- distributor
ALTER TABLE `distributor` ADD COLUMN `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志(0存在 2删除)' AFTER `update_time`;
UPDATE `distributor` SET del_flag = '0' WHERE del_flag IS NULL;

-- platform_income
ALTER TABLE `platform_income` ADD COLUMN `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志(0存在 2删除)' AFTER `create_time`;
UPDATE `platform_income` SET del_flag = '0' WHERE del_flag IS NULL;

-- transaction_record
ALTER TABLE `transaction_record` ADD COLUMN `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志(0存在 2删除)' AFTER `create_time`;
UPDATE `transaction_record` SET del_flag = '0' WHERE del_flag IS NULL;
