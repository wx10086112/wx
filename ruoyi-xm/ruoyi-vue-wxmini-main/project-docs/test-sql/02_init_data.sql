    -- ============================================
    -- 零点科技多商家SaaS团购商城 - 初始化测试数据
    -- 数据库: ruoyi-cs
    -- 前置: 先执行 01_create_tables.sql
    -- 更新: 2026-05-11
    -- ============================================

    USE `ruoyi-cs`;

    -- ============================================================
    -- 1. 商家（抽成比例直接在商家表上,后台可动态调整）
    -- ============================================================
    INSERT INTO `merchant` (`id`, `name`, `logo`, `contact`, `phone`, `commission_rate`, `status`, `balance`, `total_income`, `address`, `avatar`, `description`, `business_hours`, `product_count`, `store_count`) VALUES
    (1, '鲜果园水果店', '/profile/upload/merchant/fruit_logo.png', '张三', '13800001111', 5.00, 1, 12580.50, 58320.00, '北京市朝阳区建国路88号', '/profile/upload/merchant/fruit_cover.png', '新鲜水果,产地直供,品质保证', '08:00-22:00', 6, 2),
    (2, '好味烘焙坊', '/profile/upload/merchant/bakery_logo.png', '李四', '13800002222', 8.00, 1, 8960.00, 35600.00, '北京市海淀区中关村大街12号', '/profile/upload/merchant/bakery_cover.png', '手工烘焙,现做现卖,甜蜜每一天', '07:00-21:00', 5, 1),
    (3, '川味小厨', '/profile/upload/merchant/chuan_logo.png', '王五', '13800003333', 12.00, 1, 5200.00, 18900.00, '北京市西城区西单北大街56号', '/profile/upload/merchant/chuan_cover.png', '正宗川菜,麻辣鲜香,地道风味', '10:00-23:00', 5, 1);

    -- ============================================================
    -- 2. 商家登录账号
    -- ============================================================
    INSERT INTO `merchant_user` (`id`, `merchant_id`, `username`, `password`, `real_name`, `phone`, `role`, `status`) VALUES
    (1, 1, 'fruit_admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '张三', '13800001111', 'owner', 1),
    (2, 1, 'fruit_staff', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '赵六', '13900001111', 'member', 1),
    (3, 2, 'bakery_admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '李四', '13800002222', 'owner', 1),
    (4, 3, 'chuan_admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '王五', '13800003333', 'owner', 1);
    -- 密码统一为 123456 (BCrypt加密)

    -- ============================================================
    -- 3. 门店
    -- ============================================================
    INSERT INTO `merchant_store` (`id`, `merchant_id`, `name`, `contact`, `phone`, `address`, `longitude`, `latitude`, `business_hours`, `avatar`, `status`, `is_main`) VALUES
    (1, 1, '鲜果园(建国路总店)', '张三', '13800001111', '北京市朝阳区建国路88号', 116.4718750, 39.9150420, '08:00-22:00', '/profile/upload/store/fruit_main.jpg', 1, 1),
    (2, 1, '鲜果园(望京分店)', '赵六', '13900001111', '北京市朝阳区望京西路68号', 116.4801230, 39.9904560, '08:00-22:00', '/profile/upload/store/fruit_wj.jpg', 1, 0),
    (3, 2, '好味烘焙(中关村店)', '李四', '13800002222', '北京市海淀区中关村大街12号', 116.3168760, 39.9834520, '07:00-21:00', '/profile/upload/store/bakery_zgc.jpg', 1, 1),
    (4, 3, '川味小厨(西单店)', '王五', '13800003333', '北京市西城区西单北大街56号', 116.3739870, 39.9123450, '10:00-23:00', '/profile/upload/store/chuan_xd.jpg', 1, 1);

    -- ============================================================
    -- 4. 商品分类
    -- ============================================================
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

    -- ============================================================
    -- 5. 团购活动
    -- ============================================================
    INSERT INTO `groupon_activity` (`id`, `merchant_id`, `name`, `cover_image`, `description`, `start_time`, `end_time`, `status`, `total_sold`, `limit_per_user`) VALUES
    (1, 1, '春季水果狂欢节', '/profile/upload/groupon/spring_fruit.jpg', '春季时令水果大促,全场8折起,满100减20', '2026-04-01 00:00:00', '2026-06-30 23:59:59', 1, 356, 5),
    (2, 2, '烘焙新品尝鲜周', '/profile/upload/groupon/bakery_new.jpg', '新品面包蛋糕限时特惠,第二件半价', '2026-05-01 00:00:00', '2026-05-31 23:59:59', 1, 128, 3),
    (3, 3, '川味美食节', '/profile/upload/groupon/chuan_fest.jpg', '地道川菜团购特惠,套餐低至6折', '2026-05-10 00:00:00', '2026-06-10 23:59:59', 1, 89, 0);

    -- ============================================================
    -- 6. 商品
    -- ============================================================
    INSERT INTO `product` (`id`, `merchant_id`, `category_id`, `groupon_id`, `name`, `cover_image`, `images`, `price`, `original_price`, `stock`, `sales`, `status`, `valid_days`, `description`, `store_ids`, `sort`) VALUES
    -- 鲜果园商品
    (1, 1, 1, 1, '精选红富士苹果5斤装', '/profile/upload/product/apple_cover.jpg', '[\"/profile/upload/product/apple_1.jpg\",\"/profile/upload/product/apple_2.jpg\",\"/profile/upload/product/apple_3.jpg\"]', 29.90, 49.90, 200, 86, 1, 7, '山东烟台红富士,脆甜多汁,5斤装约12-15个', '[1,2]', 100),
    (2, 1, 1, 1, '海南金煌芒果3斤装', '/profile/upload/product/mango_cover.jpg', '[\"/profile/upload/product/mango_1.jpg\",\"/profile/upload/product/mango_2.jpg\"]', 39.90, 69.90, 150, 62, 1, 7, '海南直采金煌芒果,个大核薄,香甜可口', '[1,2]', 90),
    (3, 1, 2, 1, '智利进口车厘子1斤', '/profile/upload/product/cherry_cover.jpg', '[\"/profile/upload/product/cherry_1.jpg\",\"/profile/upload/product/cherry_2.jpg\"]', 59.90, 89.90, 80, 45, 1, 3, '智利JJ级车厘子,果径28mm+,脆甜爽口', '[1]', 80),
    (4, 1, 3, NULL, '精品水果礼盒(大)', '/profile/upload/product/box_cover.jpg', '[\"/profile/upload/product/box_1.jpg\"]', 128.00, 198.00, 50, 23, 1, 7, '精选8种时令水果,精美包装,送礼佳选', '[1,2]', 70),
    (5, 1, 1, NULL, '广西百香果10个装', '/profile/upload/product/passion_cover.jpg', '[\"/profile/upload/product/passion_1.jpg\"]', 15.90, 25.90, 300, 128, 1, 10, '广西北流百香果,酸甜可口,泡水直饮两相宜', '[1,2]', 60),
    (6, 1, 2, NULL, '泰国山竹5斤装', '/profile/upload/product/mangosteen_cover.jpg', '[\"/profile/upload/product/mangosteen_1.jpg\"]', 89.00, 139.00, 60, 15, 0, 5, '泰国进口山竹,果肉洁白,清甜多汁', '[1]', 50),
    -- 好味烘焙商品
    (7, 2, 4, 2, '招牌奶油草莓蛋糕(6寸)', '/profile/upload/product/cake_cover.jpg', '[\"/profile/upload/product/cake_1.jpg\",\"/profile/upload/product/cake_2.jpg\"]', 68.00, 98.00, 30, 42, 1, 1, '新鲜草莓+动物奶油,当日现做', '[3]', 100),
    (8, 2, 4, 2, '全麦核桃吐司', '/profile/upload/product/bread_cover.jpg', '[\"/profile/upload/product/bread_1.jpg\"]', 18.00, 28.00, 50, 88, 1, 3, '全麦面粉+新疆核桃,健康早餐首选', '[3]', 90),
    (9, 2, 5, 2, '手工蛋黄酥6个装', '/profile/upload/product/egg_cover.jpg', '[\"/profile/upload/product/egg_1.jpg\",\"/profile/upload/product/egg_2.jpg\"]', 32.00, 48.00, 100, 56, 1, 5, '酥皮+咸蛋黄+红豆沙,一口三重口感', '[3]', 80),
    (10, 2, 6, NULL, '现磨拿铁咖啡(大杯)', '/profile/upload/product/coffee_cover.jpg', '[\"/profile/upload/product/coffee_1.jpg\"]', 22.00, 32.00, 999, 167, 1, 1, '阿拉比卡咖啡豆现磨,鲜牛奶打发', '[3]', 70),
    (11, 2, 5, NULL, '蔓越莓曲奇饼干礼盒', '/profile/upload/product/cookie_cover.jpg', '[\"/profile/upload/product/cookie_1.jpg\"]', 45.00, 68.00, 80, 34, 1, 30, '进口黄油+蔓越莓干,酥脆香甜,送礼佳品', '[3]', 60),
    -- 川味小厨商品
    (12, 3, 7, 3, '招牌水煮鱼套餐', '/profile/upload/product/fish_cover.jpg', '[\"/profile/upload/product/fish_1.jpg\",\"/profile/upload/product/fish_2.jpg\"]', 68.00, 108.00, 50, 38, 1, 1, '鲜活草鱼,麻辣鲜香,含米饭+小菜', '[4]', 100),
    (13, 3, 7, 3, '麻婆豆腐套餐', '/profile/upload/product/tofu_cover.jpg', '[\"/profile/upload/product/tofu_1.jpg\"]', 28.00, 42.00, 999, 95, 1, 1, '正宗四川麻婆豆腐,麻辣烫鲜嫩,含米饭', '[4]', 90),
    (14, 3, 8, 3, '回锅肉套餐', '/profile/upload/product/pork_cover.jpg', '[\"/profile/upload/product/pork_1.jpg\"]', 38.00, 58.00, 200, 67, 1, 1, '蒜苗回锅肉,肥而不腻,含米饭+小菜', '[4]', 80),
    (15, 3, 9, NULL, '夫妻肺片', '/profile/upload/product/lung_cover.jpg', '[\"/profile/upload/product/lung_1.jpg\"]', 32.00, 48.00, 150, 43, 1, 1, '红油夫妻肺片,牛肉牛肚,麻辣鲜香', '[4]', 70),
    (16, 3, 9, NULL, '酸辣凉粉', '/profile/upload/product/jelly_cover.jpg', '[\"/profile/upload/product/jelly_1.jpg\"]', 12.00, 18.00, 300, 112, 1, 1, '手工豌豆凉粉,酸辣爽口,夏日必备', '[4]', 60);

    -- ============================================================
    -- 7. 商城用户
    -- ============================================================
    INSERT INTO `mall_user` (`id`, `nickname`, `phone`, `avatar`, `gender`, `city`, `open_id`, `status`, `total_orders`, `total_amount`) VALUES
    (1, '小明', '15000001111', 'https://thirdwx.qlogo.cn/mmopen/vi_32/abc1', 1, '北京', 'oWxUser001abc', 1, 5, 326.70),
    (2, '爱吃水果的喵', '15000002222', 'https://thirdwx.qlogo.cn/mmopen/vi_32/abc2', 2, '北京', 'oWxUser002def', 1, 3, 189.80),
    (3, '美食家老王', '15000003333', 'https://thirdwx.qlogo.cn/mmopen/vi_32/abc3', 1, '上海', 'oWxUser003ghi', 1, 8, 568.50),
    (4, '甜品控小李', '15000004444', 'https://thirdwx.qlogo.cn/mmopen/vi_32/abc4', 2, '广州', 'oWxUser004jkl', 1, 2, 136.00),
    (5, '打工人小张', '15000005555', 'https://thirdwx.qlogo.cn/mmopen/vi_32/abc5', 1, '深圳', 'oWxUser005mno', 0, 0, 0.00);

    -- ============================================================
    -- 8. 优惠券模板
    -- ============================================================
    INSERT INTO `coupon` (`id`, `merchant_id`, `name`, `type`, `discount_value`, `min_amount`, `total_count`, `used_count`, `start_time`, `end_time`, `status`) VALUES
    (1, 1, '鲜果园新客满50减10', 1, 10.00, 50.00, 500, 186, '2026-04-01 00:00:00', '2026-06-30 23:59:59', 1),
    (2, 1, '鲜果园满100减25', 1, 25.00, 100.00, 200, 78, '2026-05-01 00:00:00', '2026-05-31 23:59:59', 1),
    (3, 2, '烘焙8折券', 2, 8.00, 30.00, 300, 112, '2026-05-01 00:00:00', '2026-05-31 23:59:59', 1),
    (4, 3, '川味小厨满80减15', 1, 15.00, 80.00, 400, 95, '2026-05-10 00:00:00', '2026-06-10 23:59:59', 1),
    (5, NULL, '平台通用5元代金券', 3, 5.00, 0.00, 1000, 320, '2026-05-01 00:00:00', '2026-07-31 23:59:59', 1);

    -- ============================================================
    -- 9. 用户优惠券
    -- ============================================================
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

    -- ============================================================
    -- 10. 用户收藏
    -- ============================================================
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

    -- ============================================================
    -- 11. 用户收货地址
    -- ============================================================
    INSERT INTO `user_address` (`id`, `user_id`, `name`, `phone`, `province`, `city`, `district`, `detail`, `is_default`) VALUES
    (1, 1, '小明', '15000001111', '北京市', '北京市', '朝阳区', '建国路88号国贸大厦A座1201', 1),
    (2, 1, '小明', '15000001111', '北京市', '北京市', '海淀区', '中关村大街1号理想大厦502', 0),
    (3, 2, '喵喵', '15000002222', '北京市', '北京市', '朝阳区', '望京西路68号望京SOHO B座808', 1),
    (4, 3, '老王', '15000003333', '上海市', '上海市', '浦东新区', '陆家嘴环路1000号恒生大厦18F', 1),
    (5, 4, '小李', '15000004444', '广东省', '广州市', '天河区', '天河路385号太古汇商场L3', 1);

    -- ============================================================
    -- 12. 订单
    -- ============================================================
    INSERT INTO `mall_order` (`id`, `order_no`, `merchant_id`, `user_id`, `store_id`, `total_amount`, `pay_amount`, `commission`, `merchant_income`, `coupon_id`, `coupon_amount`, `groupon_id`, `status`, `write_off_code`, `pay_time`, `use_time`, `complete_time`, `cancel_time`, `refund_time`) VALUES
    -- 小明的订单
    (1, 'ORD20260508001', 1, 1, 1, 69.80, 59.80, 3.00, 56.80, 1, 10.00, 1, 3, 'WO20260508001', '2026-05-08 14:35:00', '2026-05-09 10:20:00', '2026-05-09 10:20:00', NULL, NULL),
    (2, 'ORD20260509002', 2, 1, 3, 68.00, 68.00, 5.44, 62.56, NULL, 0.00, 2, 1, 'WO20260509002', '2026-05-09 16:00:00', NULL, NULL, NULL, NULL),
    (3, 'ORD20260510003', 1, 1, NULL, 128.00, 128.00, 6.40, 121.60, NULL, 0.00, NULL, 0, '', NULL, NULL, NULL, NULL, NULL),
    -- 喵喵的订单
    (4, 'ORD20260509001', 2, 2, 3, 50.00, 42.00, 3.36, 38.64, 3, 8.00, 2, 3, 'WO20260509004', '2026-05-09 11:05:00', '2026-05-09 15:30:00', '2026-05-09 15:30:00', NULL, NULL),
    (5, 'ORD20260510004', 1, 2, 1, 55.80, 55.80, 2.79, 53.01, NULL, 0.00, 1, 1, 'WO20260510005', '2026-05-10 09:30:00', NULL, NULL, NULL, NULL),
    -- 美食家老王的订单
    (6, 'ORD20260510001', 3, 3, 4, 108.00, 93.00, 11.16, 81.84, 4, 15.00, 3, 2, 'WO20260510006', '2026-05-10 19:35:00', '2026-05-10 20:10:00', NULL, NULL, NULL),
    (7, 'ORD20260507005', 1, 3, 2, 89.80, 89.80, 4.49, 85.31, NULL, 0.00, 1, 3, 'WO20260507007', '2026-05-07 11:00:00', '2026-05-07 16:00:00', '2026-05-07 16:00:00', NULL, NULL),
    (8, 'ORD20260506006', 3, 3, NULL, 44.00, 44.00, 5.28, 38.72, NULL, 0.00, NULL, 5, '', NULL, NULL, NULL, '2026-05-06 12:30:00', NULL),
    -- 甜品控小李的订单
    (9, 'ORD20260506007', 2, 4, 3, 113.00, 113.00, 9.04, 103.96, NULL, 0.00, 2, 3, 'WO20260506009', '2026-05-06 17:30:00', '2026-05-06 18:00:00', '2026-05-06 18:00:00', NULL, NULL),
    (10, 'ORD20260504008', 2, 4, NULL, 45.00, 45.00, 3.60, 41.40, NULL, 0.00, NULL, 4, 'WO20260504010', '2026-05-04 14:00:00', NULL, NULL, NULL, '2026-05-05 10:00:00');

    -- ============================================================
    -- 13. 订单商品明细
    -- ============================================================
    INSERT INTO `order_item` (`id`, `order_id`, `order_no`, `merchant_id`, `product_id`, `product_name`, `product_image`, `price`, `quantity`, `subtotal`) VALUES
    -- 订单1: 小明买苹果+芒果
    (1, 1, 'ORD20260508001', 1, 1, '精选红富士苹果5斤装', '/profile/upload/product/apple_cover.jpg', 29.90, 1, 29.90),
    (2, 1, 'ORD20260508001', 1, 2, '海南金煌芒果3斤装', '/profile/upload/product/mango_cover.jpg', 39.90, 1, 39.90),
    -- 订单2: 小明买草莓蛋糕
    (3, 2, 'ORD20260509002', 2, 7, '招牌奶油草莓蛋糕(6寸)', '/profile/upload/product/cake_cover.jpg', 68.00, 1, 68.00),
    -- 订单3: 小明的购物车待支付
    (4, 3, 'ORD20260510003', 1, 4, '精品水果礼盒(大)', '/profile/upload/product/box_cover.jpg', 128.00, 1, 128.00),
    -- 订单4: 喵喵买蛋黄酥+吐司
    (5, 4, 'ORD20260509001', 2, 9, '手工蛋黄酥6个装', '/profile/upload/product/egg_cover.jpg', 32.00, 1, 32.00),
    (6, 4, 'ORD20260509001', 2, 8, '全麦核桃吐司', '/profile/upload/product/bread_cover.jpg', 18.00, 1, 18.00),
    -- 订单5: 喵喵买苹果+百香果
    (7, 5, 'ORD20260510004', 1, 1, '精选红富士苹果5斤装', '/profile/upload/product/apple_cover.jpg', 29.90, 1, 29.90),
    (8, 5, 'ORD20260510004', 1, 5, '广西百香果10个装', '/profile/upload/product/passion_cover.jpg', 15.90, 1, 15.90),
    -- 订单6: 老王的水煮鱼+回锅肉
    (9, 6, 'ORD20260510001', 3, 12, '招牌水煮鱼套餐', '/profile/upload/product/fish_cover.jpg', 68.00, 1, 68.00),
    (10, 6, 'ORD20260510001', 3, 14, '回锅肉套餐', '/profile/upload/product/pork_cover.jpg', 38.00, 1, 38.00),
    -- 订单7: 老王买车厘子+芒果
    (11, 7, 'ORD20260507005', 1, 3, '智利进口车厘子1斤', '/profile/upload/product/cherry_cover.jpg', 59.90, 1, 59.90),
    (12, 7, 'ORD20260507005', 1, 2, '海南金煌芒果3斤装', '/profile/upload/product/mango_cover.jpg', 29.90, 1, 29.90),
    -- 订单8: 老王取消的订单(夫妻肺片+麻婆豆腐)
    (13, 8, 'ORD20260506006', 3, 15, '夫妻肺片', '/profile/upload/product/lung_cover.jpg', 32.00, 1, 32.00),
    (14, 8, 'ORD20260506006', 3, 13, '麻婆豆腐套餐', '/profile/upload/product/tofu_cover.jpg', 12.00, 1, 12.00),
    -- 订单9: 小李买蛋糕+蛋黄酥+拿铁
    (15, 9, 'ORD20260506007', 2, 7, '招牌奶油草莓蛋糕(6寸)', '/profile/upload/product/cake_cover.jpg', 68.00, 1, 68.00),
    (16, 9, 'ORD20260506007', 2, 9, '手工蛋黄酥6个装', '/profile/upload/product/egg_cover.jpg', 32.00, 1, 32.00),
    (17, 9, 'ORD20260506007', 2, 10, '现磨拿铁咖啡(大杯)', '/profile/upload/product/coffee_cover.jpg', 22.00, 1, 22.00),
    -- 订单10: 小李退款的订单(曲奇)
    (18, 10, 'ORD20260504008', 2, 11, '蔓越莓曲奇饼干礼盒', '/profile/upload/product/cookie_cover.jpg', 45.00, 1, 45.00);

    -- ============================================================
    -- 14. 购物车
    -- ============================================================
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

    -- ============================================================
    -- 15. 首页轮播图
    -- ============================================================
    INSERT INTO `banner` (`id`, `title`, `image`, `link_type`, `link_id`, `link_url`, `sort`, `status`, `position`) VALUES
    (1, '春季水果狂欢节', '/profile/upload/banner/spring_fruit.jpg', 2, 1, '', 100, 1, 'home'),
    (2, '新品烘焙尝鲜', '/profile/upload/banner/bakery_new.jpg', 2, 2, '', 90, 1, 'home'),
    (3, '川味美食节开吃啦', '/profile/upload/banner/chuan_fest.jpg', 2, 3, '', 80, 1, 'home'),
    (4, '车厘子限时特价', '/profile/upload/banner/cherry_sale.jpg', 1, 3, '', 70, 1, 'home'),
    (5, '优惠券大放送', '/profile/upload/banner/coupon_banner.jpg', 0, NULL, '', 60, 1, 'home'),
    (6, '5元无门槛券', '/profile/upload/banner/coupon_5yuan.jpg', 0, NULL, '', 100, 1, 'coupon');

    -- ============================================================
    -- 16. 支付记录
    -- ============================================================
    INSERT INTO `payment_record` (`id`, `order_no`, `merchant_id`, `user_id`, `amount`, `pay_type`, `transaction_id`, `out_trade_no`, `pay_status`, `pay_time`, `notify_result`) VALUES
    (1, 'ORD20260508001', 1, 1, 59.80, 'wechat', 'WX20260508143500001', 'MCH_ORD20260508001', 1, '2026-05-08 14:35:00', 'success'),
    (2, 'ORD20260509002', 2, 1, 68.00, 'wechat', 'WX20260509160000002', 'MCH_ORD20260509002', 1, '2026-05-09 16:00:00', 'success'),
    (3, 'ORD20260509001', 2, 2, 42.00, 'wechat', 'WX20260509110500003', 'MCH_ORD20260509001', 1, '2026-05-09 11:05:00', 'success'),
    (4, 'ORD20260510004', 1, 2, 55.80, 'wechat', 'WX20260510093000004', 'MCH_ORD20260510004', 1, '2026-05-10 09:30:00', 'success'),
    (5, 'ORD20260510001', 3, 3, 93.00, 'wechat', 'WX20260510193500005', 'MCH_ORD20260510001', 1, '2026-05-10 19:35:00', 'success'),
    (6, 'ORD20260507005', 1, 3, 89.80, 'wechat', 'WX20260507110000006', 'MCH_ORD20260507005', 1, '2026-05-07 11:00:00', 'success'),
    (7, 'ORD20260506007', 2, 4, 113.00, 'wechat', 'WX20260506173000007', 'MCH_ORD20260506007', 1, '2026-05-06 17:30:00', 'success'),
    (8, 'ORD20260504008', 2, 4, 45.00, 'wechat', 'WX20260504140000008', 'MCH_ORD20260504008', 3, '2026-05-04 14:00:00', 'success');

    -- ============================================================
    -- 17. 资金流水
    -- ============================================================
    INSERT INTO `transaction_record` (`id`, `merchant_id`, `type`, `amount`, `balance`, `order_no`, `description`) VALUES
    -- 用户支付记录
    (NULL, NULL, 'payment', -59.80, NULL, 'ORD20260508001', '用户支付订单ORD20260508001'),
    (NULL, NULL, 'payment', -68.00, NULL, 'ORD20260509002', '用户支付订单ORD20260509002'),
    (NULL, NULL, 'payment', -42.00, NULL, 'ORD20260509001', '用户支付订单ORD20260509001'),
    (NULL, NULL, 'payment', -55.80, NULL, 'ORD20260510004', '用户支付订单ORD20260510004'),
    (NULL, NULL, 'payment', -93.00, NULL, 'ORD20260510001', '用户支付订单ORD20260510001'),
    (NULL, NULL, 'payment', -89.80, NULL, 'ORD20260507005', '用户支付订单ORD20260507005'),
    (NULL, NULL, 'payment', -113.00, NULL, 'ORD20260506007', '用户支付订单ORD20260506007'),
    -- 商家收入记录
    (NULL, 1, 'income', 56.80, 12580.50, 'ORD20260508001', '商家收入-订单ORD20260508001'),
    (NULL, 2, 'income', 62.56, 8960.00, 'ORD20260509002', '商家收入-订单ORD20260509002'),
    (NULL, 2, 'income', 38.64, 8960.00, 'ORD20260509001', '商家收入-订单ORD20260509001'),
    (NULL, 1, 'income', 53.01, 12580.50, 'ORD20260510004', '商家收入-订单ORD20260510004'),
    (NULL, 3, 'income', 81.84, 5200.00, 'ORD20260510001', '商家收入-订单ORD20260510001'),
    (NULL, 1, 'income', 85.31, 12580.50, 'ORD20260507005', '商家收入-订单ORD20260507005'),
    (NULL, 2, 'income', 103.96, 8960.00, 'ORD20260506007', '商家收入-订单ORD20260506007'),
    -- 退款记录
    (NULL, 2, 'refund', -45.00, 8960.00, 'ORD20260504008', '退款-订单ORD20260504008');

    -- ============================================================
    -- 18. 退款记录
    -- ============================================================
    INSERT INTO `refund_record` (`id`, `order_no`, `refund_no`, `merchant_id`, `user_id`, `payment_record_id`, `refund_amount`, `refund_reason`, `refund_type`, `status`, `audit_time`, `refund_time`, `reject_reason`, `operator`) VALUES
    (1, 'ORD20260504008', 'RF20260505001', 2, 4, 8, 45.00, '商品与描述不符,口味不满意', 1, 3, '2026-05-05 09:30:00', '2026-05-05 10:00:00', '', 'admin');

    -- ============================================================
    -- 19. 平台收益
    -- ============================================================
    INSERT INTO `platform_income` (`id`, `merchant_id`, `order_no`, `order_amount`, `commission_rate`, `commission`) VALUES
    (1, 1, 'ORD20260508001', 59.80, 5.00, 3.00),
    (2, 2, 'ORD20260509002', 68.00, 8.00, 5.44),
    (3, 2, 'ORD20260509001', 42.00, 8.00, 3.36),
    (4, 1, 'ORD20260510004', 55.80, 5.00, 2.79),
    (5, 3, 'ORD20260510001', 93.00, 12.00, 11.16),
    (6, 1, 'ORD20260507005', 89.80, 5.00, 4.49),
    (7, 2, 'ORD20260506007', 113.00, 8.00, 9.04);

    -- ============================================================
    -- 20. 提现记录
    -- ============================================================
    INSERT INTO `withdraw_record` (`id`, `merchant_id`, `amount`, `bank_name`, `bank_account`, `account_name`, `status`, `audit_time`, `pay_time`, `reject_reason`) VALUES
    (1, 1, 5000.00, '中国工商银行', '6222021234567890123', '张三', 2, '2026-05-05 10:00:00', '2026-05-05 14:00:00', ''),
    (2, 2, 3000.00, '中国建设银行', '6227001234567890456', '李四', 2, '2026-05-06 09:00:00', '2026-05-06 15:00:00', ''),
    (3, 3, 2000.00, '中国银行', '6217001234567890789', '王五', 1, '2026-05-10 10:00:00', NULL, ''),
    (4, 1, 2000.00, '中国工商银行', '6222021234567890123', '张三', 0, NULL, NULL, '');

    -- ============================================================
    -- 21. 商家账单
    -- ============================================================
    INSERT INTO `merchant_bill` (`id`, `merchant_id`, `bill_no`, `bill_type`, `start_date`, `end_date`, `total_orders`, `total_amount`, `total_commission`, `net_income`, `status`, `settle_time`) VALUES
    (1, 1, 'BILL20260501M01', 'monthly', '2026-05-01', '2026-05-31', 3, 277.60, 13.88, 263.72, 0, NULL),
    (2, 2, 'BILL20260501M02', 'monthly', '2026-05-01', '2026-05-31', 3, 223.00, 17.84, 205.16, 0, NULL),
    (3, 3, 'BILL20260501M03', 'monthly', '2026-05-01', '2026-05-31', 1, 93.00, 11.16, 81.84, 0, NULL);

    -- ============================================================
    -- 22. 操作日志(通用)
    -- ============================================================
    INSERT INTO `operation_log` (`id`, `operator`, `operator_type`, `module`, `action`, `method`, `ip`, `status`, `cost_time`, `oper_time`) VALUES
    (1, 'admin', 'admin', '商家管理', '审核商家', 'com.ruoyi.merchant.controller.MerchantController.audit()', '192.168.1.100', 0, 120, '2026-05-01 09:00:00'),
    (2, 'admin', 'admin', '订单管理', '查询订单列表', 'com.ruoyi.order.controller.OrderController.list()', '192.168.1.100', 0, 85, '2026-05-10 10:30:00'),
    (3, 'fruit_admin', 'merchant', '商品管理', '上架商品', 'com.ruoyi.merchant.controller.ProductController.onShelf()', '192.168.1.101', 0, 95, '2026-05-02 14:00:00'),
    (4, 'bakery_admin', 'merchant', '订单管理', '核销订单', 'com.ruoyi.merchant.controller.OrderController.writeOff()', '192.168.1.102', 0, 110, '2026-05-09 15:30:00'),
    (5, 'admin', 'admin', '财务管理', '审核提现', 'com.ruoyi.finance.controller.WithdrawController.audit()', '192.168.1.100', 0, 200, '2026-05-06 09:00:00');

    -- ============================================================
    -- 23. 登录日志(运营后台)
    -- ============================================================
    INSERT INTO `mall_login_log` (`id`, `user_name`, `ip`, `location`, `browser`, `os`, `status`, `msg`, `login_time`) VALUES
    (1, 'admin', '192.168.1.100', '北京市', 'Chrome 120', 'Windows 11', 0, '登录成功', '2026-05-10 08:30:00'),
    (2, 'admin', '192.168.1.100', '北京市', 'Chrome 120', 'Windows 11', 0, '登录成功', '2026-05-09 08:45:00'),
    (3, 'ry', '192.168.1.105', '上海市', 'Edge 120', 'Windows 10', 0, '登录成功', '2026-05-10 09:00:00'),
    (4, 'admin', '10.0.0.1', '未知', 'Firefox 115', 'Mac OS', 1, '密码错误', '2026-05-10 03:15:00'),
    (5, 'admin', '192.168.1.100', '北京市', 'Chrome 120', 'Windows 11', 0, '登录成功', '2026-05-08 09:10:00');

    -- ============================================================
    -- 24. 操作日志(运营后台)
    -- ============================================================
    INSERT INTO `mall_oper_log` (`id`, `operator`, `module`, `operation`, `method`, `ip`, `status`, `cost_time`, `oper_time`) VALUES
    (1, 'admin', '商家管理', '审核通过商家"川味小厨"', 'com.ruoyi.merchant.controller.MerchantController.audit()', '192.168.1.100', 0, 150, '2026-05-01 09:00:00'),
    (2, 'admin', '订单管理', '导出订单报表', 'com.ruoyi.order.controller.OrderController.export()', '192.168.1.100', 0, 2300, '2026-05-10 10:30:00'),
    (3, 'admin', '财务管理', '通过提现申请#3', 'com.ruoyi.finance.controller.WithdrawController.approve()', '192.168.1.100', 0, 180, '2026-05-10 10:00:00'),
    (4, 'ry', '轮播图管理', '新增轮播图"川味美食节"', 'com.ruoyi.content.controller.BannerController.add()', '192.168.1.105', 0, 95, '2026-05-10 09:30:00'),
    (5, 'admin', '优惠券管理', '新增优惠券"平台通用5元代金券"', 'com.ruoyi.coupon.controller.CouponController.add()', '192.168.1.100', 0, 110, '2026-05-01 10:00:00');
