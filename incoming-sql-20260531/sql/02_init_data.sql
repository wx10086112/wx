-- ============================================
-- 零点科技多商家SaaS团购商城 - 初始化测试数据
-- 数据库: ruoyi-cs
-- 执行前必须先建表（01_all_in_one.sql）
-- ============================================

USE `ruoyi-cs`;

-- ==================== 分销商 ====================
INSERT INTO `distributor` (`id`, `name`, `contact`, `phone`, `region_code`, `region_name`, `status`, `del_flag`) VALUES
(1, '默认分销商', '系统', '13800000001', '000000', '全国', 1, '0');

-- ==================== 商家 ====================
INSERT INTO `merchant` (`id`, `distributor_id`, `name`, `contact`, `phone`, `status`, `commission_rate`, `description`, `del_flag`) VALUES
(1, 1, '默认测试商家', '张经理', '13800001111', 1, 10.00, '测试用商家', '0');

-- ==================== 商家门店 ====================
INSERT INTO `merchant_store` (`id`, `merchant_id`, `name`, `contact`, `phone`, `address`, `is_main`, `status`, `del_flag`) VALUES
(1, 1, '默认门店', '张经理', '13800001111', '测试地址', 1, 1, '0');

-- ==================== 商家端员工 ====================
-- 密码: 123456 (BCrypt)
INSERT INTO `merchant_user` (`id`, `merchant_id`, `username`, `password`, `real_name`, `role`, `status`, `del_flag`) VALUES
(1, 1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iItTVnFm', '管理员', 'owner', 1, '0');

-- ==================== 商品分类 ====================
INSERT INTO `product_category` (`id`, `merchant_id`, `name`, `sort`, `status`, `del_flag`) VALUES
(1, 1, '热门推荐', 100, 1, '0'),
(2, 1, '优惠套餐', 90, 1, '0');

-- ==================== 商品 ====================
INSERT INTO `product` (`id`, `merchant_id`, `category_id`, `name`, `description`, `price`, `original_price`, `stock`, `status`, `sales`, `valid_days`, `del_flag`) VALUES
(1, 1, 1, '经典体验套餐', '限时优惠', 99.00, 198.00, 100, 1, 50, 30, '0'),
(2, 1, 2, 'VIP尊享套餐', '含全部服务', 198.00, 398.00, 50, 1, 30, 90, '0');

-- ==================== C端用户 ====================
INSERT INTO `mall_user` (`id`, `open_id`, `nick_name`, `phone`, `del_flag`) VALUES
(1, 'test_openid_001', '测试用户', '13800001111', '0');

-- ==================== 运营后台用户 ====================
-- 密码: admin123 (BCrypt)
INSERT INTO `sys_user` (`user_id`, `user_name`, `password`, `nick_name`, `status`, `account_type`, `del_flag`) VALUES
(1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '超级管理员', '0', 'PLATFORM', '0'),
(2, 'distributor', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '分销商管理员', '0', 'DISTRIBUTOR_ADMIN', '0');

-- ==================== 角色 ====================
INSERT INTO `sys_role` (`role_id`, `role_name`, `role_key`, `role_sort`, `status`, `del_flag`) VALUES
(1, '超级管理员', 'admin', 1, '0', '0'),
(2, '普通角色', 'common', 2, '0', '0'),
(3, '分销商管理员', 'distributor_admin', 3, '0', '0');

-- ==================== 用户-角色关联 ====================
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1),
(2, 3);
