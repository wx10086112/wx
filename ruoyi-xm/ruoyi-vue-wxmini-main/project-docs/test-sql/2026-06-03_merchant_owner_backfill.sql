-- ============================================
-- 商家店长账号补录
-- 数据库: ruoyi-cs
-- 说明: 为缺少 owner 账号的商家补齐默认超级用户
-- 默认密码: 123456
-- ============================================

USE `ruoyi-cs`;

INSERT INTO `merchant_user` (`merchant_id`, `username`, `password`, `real_name`, `phone`, `role`, `status`, `remark`, `create_time`) VALUES
(13, 'merchant_13_owner', '$2a$10$huMDrW826DDn5eb/.GEoTOoXWKQfHkqCp2/7GDQw6w9zpEPkeRnA.', '赵经理', '13800001313', 'owner', 1, '系统自动补录的商家超级用户', NOW()),
(14, 'merchant_14_owner', '$2a$10$huMDrW826DDn5eb/.GEoTOoXWKQfHkqCp2/7GDQw6w9zpEPkeRnA.', '李经理', '13800001414', 'owner', 1, '系统自动补录的商家超级用户', NOW()),
(15, 'merchant_15_owner', '$2a$10$huMDrW826DDn5eb/.GEoTOoXWKQfHkqCp2/7GDQw6w9zpEPkeRnA.', '周经理', '13800001515', 'owner', 1, '系统自动补录的商家超级用户', NOW()),
(16, 'merchant_16_owner', '$2a$10$huMDrW826DDn5eb/.GEoTOoXWKQfHkqCp2/7GDQw6w9zpEPkeRnA.', '钱经理', '13800001616', 'owner', 1, '系统自动补录的商家超级用户', NOW()),
(17, 'merchant_17_owner', '$2a$10$huMDrW826DDn5eb/.GEoTOoXWKQfHkqCp2/7GDQw6w9zpEPkeRnA.', '吴经理', '13800001717', 'owner', 1, '系统自动补录的商家超级用户', NOW()),
(18, 'merchant_18_owner', '$2a$10$huMDrW826DDn5eb/.GEoTOoXWKQfHkqCp2/7GDQw6w9zpEPkeRnA.', '陈经理', '13800001818', 'owner', 1, '系统自动补录的商家超级用户', NOW()),
(19, 'merchant_19_owner', '$2a$10$huMDrW826DDn5eb/.GEoTOoXWKQfHkqCp2/7GDQw6w9zpEPkeRnA.', '林经理', '13800001919', 'owner', 1, '系统自动补录的商家超级用户', NOW()),
(20, 'merchant_20_owner', '$2a$10$huMDrW826DDn5eb/.GEoTOoXWKQfHkqCp2/7GDQw6w9zpEPkeRnA.', '黄经理', '13800002020', 'owner', 1, '系统自动补录的商家超级用户', NOW()),
(21, 'merchant_21_owner', '$2a$10$huMDrW826DDn5eb/.GEoTOoXWKQfHkqCp2/7GDQw6w9zpEPkeRnA.', '郑经理', '13800002121', 'owner', 1, '系统自动补录的商家超级用户', NOW()),
(22, 'merchant_22_owner', '$2a$10$huMDrW826DDn5eb/.GEoTOoXWKQfHkqCp2/7GDQw6w9zpEPkeRnA.', '冯经理', '13800002222', 'owner', 1, '系统自动补录的商家超级用户', NOW()),
(23, 'merchant_23_owner', '$2a$10$huMDrW826DDn5eb/.GEoTOoXWKQfHkqCp2/7GDQw6w9zpEPkeRnA.', '孙经理', '13800002323', 'owner', 1, '系统自动补录的商家超级用户', NOW()),
(24, 'merchant_24_owner', '$2a$10$huMDrW826DDn5eb/.GEoTOoXWKQfHkqCp2/7GDQw6w9zpEPkeRnA.', '马经理', '13800002424', 'owner', 1, '系统自动补录的商家超级用户', NOW()),
(25, 'merchant_25_owner', '$2a$10$huMDrW826DDn5eb/.GEoTOoXWKQfHkqCp2/7GDQw6w9zpEPkeRnA.', '朱经理', '13800002525', 'owner', 1, '系统自动补录的商家超级用户', NOW()),
(26, 'merchant_26_owner', '$2a$10$huMDrW826DDn5eb/.GEoTOoXWKQfHkqCp2/7GDQw6w9zpEPkeRnA.', '杨经理', '13800002626', 'owner', 1, '系统自动补录的商家超级用户', NOW()),
(27, 'merchant_27_owner', '$2a$10$huMDrW826DDn5eb/.GEoTOoXWKQfHkqCp2/7GDQw6w9zpEPkeRnA.', '高经理', '13800002727', 'owner', 1, '系统自动补录的商家超级用户', NOW());
