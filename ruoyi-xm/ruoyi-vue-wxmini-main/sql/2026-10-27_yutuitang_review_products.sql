-- ============================================================
-- 裕推堂（merchant_id = 5）审核占位商品 + 品牌形象
-- 目的：让小程序首页/商品详情/预点单有可体验内容，便于微信审核
-- 说明：幂等，可重复执行；正式商品上线后可整批下架或替换
-- ============================================================
USE `ruoyi-cs`;

-- 1) 服务分类
INSERT INTO product_category (merchant_id, name, sort, status, del_flag, create_time, update_time)
SELECT 5, '调理服务', 0, 1, '0', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM product_category WHERE merchant_id = 5 AND name = '调理服务');

SET @cat := (SELECT id FROM product_category WHERE merchant_id = 5 AND name = '调理服务' LIMIT 1);

-- 2) 6 个占位服务（六店通用，store_ids 留空）
INSERT INTO product (merchant_id, category_id, name, cover_image, main_image, images, price, original_price,
                     stock, sales, status, verify_type, valid_days, verify_notice, description, store_ids, sort,
                     create_time, update_time)
SELECT 5, @cat, '肩颈深度调理（60分钟）',
       '/profile/merchant_images/5/product/temp/main_ytt_1.png',
       '/profile/merchant_images/5/product/temp/main_ytt_1.png', '',
       99.00, 168.00, 200, 0, 1, 1, 365,
       '六店通用 · 建议提前2小时预约',
       '专业调理师一对一，针对肩颈僵硬、酸胀进行深度放松。购买后到店出示核销码即可使用。',
       '', 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM product WHERE merchant_id = 5 AND name = '肩颈深度调理（60分钟）' AND del_flag = '0');

INSERT INTO product (merchant_id, category_id, name, cover_image, main_image, images, price, original_price,
                     stock, sales, status, verify_type, valid_days, verify_notice, description, store_ids, sort,
                     create_time, update_time)
SELECT 5, @cat, '腰背舒缓调理（60分钟）',
       '/profile/merchant_images/5/product/temp/main_ytt_2.png',
       '/profile/merchant_images/5/product/temp/main_ytt_2.png', '',
       129.00, 199.00, 200, 0, 1, 1, 365,
       '六店通用 · 建议提前2小时预约',
       '针对久坐、弯腰劳损引起的腰背疲劳，调理师一对一舒缓放松。到店出示核销码即可使用。',
       '', 2, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM product WHERE merchant_id = 5 AND name = '腰背舒缓调理（60分钟）' AND del_flag = '0');

INSERT INTO product (merchant_id, category_id, name, cover_image, main_image, images, price, original_price,
                     stock, sales, status, verify_type, valid_days, verify_notice, description, store_ids, sort,
                     create_time, update_time)
SELECT 5, @cat, '全身经络推拿（90分钟）',
       '/profile/merchant_images/5/product/temp/main_ytt_3.png',
       '/profile/merchant_images/5/product/temp/main_ytt_3.png', '',
       199.00, 299.00, 200, 0, 1, 1, 365,
       '六店通用 · 建议提前2小时预约',
       '90分钟全身经络推拿，舒缓全身紧张与疲劳，适合长期伏案人群。到店出示核销码即可使用。',
       '', 3, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM product WHERE merchant_id = 5 AND name = '全身经络推拿（90分钟）' AND del_flag = '0');

INSERT INTO product (merchant_id, category_id, name, cover_image, main_image, images, price, original_price,
                     stock, sales, status, verify_type, valid_days, verify_notice, description, store_ids, sort,
                     create_time, update_time)
SELECT 5, @cat, '古法艾灸调理（60分钟）',
       '/profile/merchant_images/5/product/temp/main_ytt_4.png',
       '/profile/merchant_images/5/product/temp/main_ytt_4.png', '',
       79.00, 128.00, 200, 0, 1, 1, 365,
       '六店通用 · 建议提前2小时预约',
       '古法艾灸温经散寒，适合手脚发凉、易疲劳人群。到店出示核销码即可使用。',
       '', 4, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM product WHERE merchant_id = 5 AND name = '古法艾灸调理（60分钟）' AND del_flag = '0');

INSERT INTO product (merchant_id, category_id, name, cover_image, main_image, images, price, original_price,
                     stock, sales, status, verify_type, valid_days, verify_notice, description, store_ids, sort,
                     create_time, update_time)
SELECT 5, @cat, '拔罐祛湿调理（40分钟）',
       '/profile/merchant_images/5/product/temp/main_ytt_5.png',
       '/profile/merchant_images/5/product/temp/main_ytt_5.png', '',
       59.00, 98.00, 200, 0, 1, 1, 365,
       '六店通用 · 建议提前2小时预约',
       '拔罐祛湿通络，缓解身体沉重与疲劳感。到店出示核销码即可使用。',
       '', 5, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM product WHERE merchant_id = 5 AND name = '拔罐祛湿调理（40分钟）' AND del_flag = '0');

INSERT INTO product (merchant_id, category_id, name, cover_image, main_image, images, price, original_price,
                     stock, sales, status, verify_type, valid_days, verify_notice, description, store_ids, sort,
                     create_time, update_time)
SELECT 5, @cat, '刮痧排毒调理（40分钟）',
       '/profile/merchant_images/5/product/temp/main_ytt_6.png',
       '/profile/merchant_images/5/product/temp/main_ytt_6.png', '',
       69.00, 108.00, 200, 0, 1, 1, 365,
       '六店通用 · 建议提前2小时预约',
       '刮痧疏通经络，适合肩颈腰背酸胀人群。到店出示核销码即可使用。',
       '', 6, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM product WHERE merchant_id = 5 AND name = '刮痧排毒调理（40分钟）' AND del_flag = '0');

-- 3) 商家品牌形象（logo/头像/首页轮播/简介）
UPDATE merchant
SET logo = '/profile/merchant_images/5/logo.jpg',
    avatar = '/profile/merchant_images/5/logo.jpg',
    carousel_images = '/profile/merchant_images/5/product/temp/banner.png,/profile/merchant_images/5/product/temp/main_ytt_1.png,/profile/merchant_images/5/product/temp/main_ytt_3.png',
    description = '裕推堂筋骨调理，六店通用，专业调理师一对一服务。',
    business_hours = '10:00-21:00',
    product_count = (SELECT COUNT(1) FROM product WHERE merchant_id = 5 AND del_flag = '0' AND status = 1),
    update_time = NOW()
WHERE id = 5;

-- 4) 校验
-- SELECT id, name, price, original_price, stock, status FROM product WHERE merchant_id = 5 AND del_flag = '0' ORDER BY sort;
-- SELECT id, name, logo, avatar, carousel_images, product_count FROM merchant WHERE id = 5;
