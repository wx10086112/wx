-- 角色权限适配分销商层级
-- sys_role 增加角色归属字段
ALTER TABLE `sys_role` ADD COLUMN `role_scope` VARCHAR(20) DEFAULT 'PLATFORM' COMMENT '角色归属: PLATFORM/DISTRIBUTOR/MERCHANT';

-- sys_user 增加账号类型和业务绑定字段
ALTER TABLE `sys_user` ADD COLUMN `account_type` VARCHAR(20) DEFAULT 'PLATFORM' COMMENT '账号类型: PLATFORM/DISTRIBUTOR/MERCHANT';
ALTER TABLE `sys_user` ADD COLUMN `distributor_id` BIGINT DEFAULT NULL COMMENT '绑定分销商ID';
ALTER TABLE `sys_user` ADD COLUMN `merchant_id` BIGINT DEFAULT NULL COMMENT '绑定商家ID';
