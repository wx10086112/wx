-- 分销商表
CREATE TABLE IF NOT EXISTS `distributor` (
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

-- merchant 表增加 distributor_id
ALTER TABLE `merchant` ADD COLUMN `distributor_id` BIGINT DEFAULT NULL COMMENT '所属分销商ID' AFTER `id`;
ALTER TABLE `merchant` ADD KEY `idx_distributor_id` (`distributor_id`);

-- 核心业务表增加 distributor_id（如已存在则跳过）
-- 以下语句如果报错"Duplicate column"可忽略

-- groupon_activity
ALTER TABLE `groupon_activity` ADD COLUMN `distributor_id` BIGINT DEFAULT NULL COMMENT '分销商ID' AFTER `merchant_id`;
ALTER TABLE `groupon_activity` ADD KEY `idx_distributor_id` (`distributor_id`);

-- groupon_activity_item
ALTER TABLE `groupon_activity_item` ADD COLUMN `distributor_id` BIGINT DEFAULT NULL COMMENT '分销商ID' AFTER `merchant_id`;
ALTER TABLE `groupon_activity_item` ADD KEY `idx_distributor_id` (`distributor_id`);

-- ============================================================
-- 分销商测试数据（需先执行 12_role_user_biz_scope.sql 补字段）
-- ============================================================

-- 1. 分销商业务数据
INSERT INTO `distributor` (`id`, `name`, `contact`, `phone`, `username`, `password`, `region_code`, `region_name`, `status`, `remark`)
VALUES (1, '华东分销商', '张三', '13800001111', 'dist_east', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'EAST', '华东区域', 1, '测试分销商')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- 2. 分销商后台账号（密码 123456 的 BCrypt 值）
INSERT INTO `sys_user` (`user_id`, `dept_id`, `user_name`, `nick_name`, `email`, `phone`, `sex`, `password`, `status`, `create_time`, `account_type`, `distributor_id`, `merchant_id`)
VALUES (1001, 100, 'dist_east', '华东分销商管理员', 'dist_east@test.com', '13800001111', '0', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', NOW(), 'DISTRIBUTOR', 1, NULL)
ON DUPLICATE KEY UPDATE `account_type` = 'DISTRIBUTOR', `distributor_id` = 1;

-- 3. 分销商管理员角色
INSERT INTO `sys_role` (`role_id`, `role_name`, `role_key`, `role_sort`, `data_scope`, `status`, `create_time`, `role_scope`)
VALUES (100, '分销商管理员', 'DISTRIBUTOR_ADMIN', 10, '5', '0', NOW(), 'DISTRIBUTOR')
ON DUPLICATE KEY UPDATE `role_scope` = 'DISTRIBUTOR';

-- 4. 绑定账号和角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1001, 100)
ON DUPLICATE KEY UPDATE `user_id` = VALUES(`user_id`);

-- 5. 给分销商角色分配基础菜单（工作台+商家+订单+团购+结算）
-- 需要根据实际 sys_menu 中的 menu_id 调整，以下为示例
-- INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (100, 2000), (100, 2001), (100, 2002);
