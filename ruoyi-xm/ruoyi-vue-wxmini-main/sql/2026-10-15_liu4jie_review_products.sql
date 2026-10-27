-- 2026-10-15 刘四姐卤肉店（merchant_id = 3）占位商品
--
-- 背景：该商家小程序首次提审需要在店铺内有可展示的商品内容，
--       审核通过后这些占位商品可以整批下架或删除（见文末清理语句）。
-- 图片：6 张商品图已复制到 /data/ruoyi/uploadPath/merchant_images/3/product/temp/，
--       缩略图由后端 MerchantImageController 按需生成，无需额外处理。
-- 分类：复用该商家已有分类“肉”(7) 与“饮品”(6)。
-- 幂等：按 (merchant_id, name) 判重，可重复执行，不会产生重复商品。
-- 备注：商品上架后 /wxmini/groupon/list 会自动把商品同步成预点单服务
--       (mall_booking_service，remark = '由上架商品自动生成预点单服务')，
--       因此无需手工维护服务表。

SET NAMES utf8mb4;

INSERT INTO product (merchant_id, category_id, name, cover_image, main_image, price, original_price,
                     stock, sales, status, verify_type, valid_days, description, sort,
                     create_by, create_time, update_time, remark, del_flag)
SELECT 3, 7, '【招牌】麻辣小龙虾（约1斤）',
       '/profile/merchant_images/3/product/temp/main_f4fb59e3.jpg',
       '/profile/merchant_images/3/product/temp/main_f4fb59e3.jpg',
       58.00, 78.00, 200, 0, 1, 1, 30, '每日现做，香辣入味，堂食外带均可。', 1,
       'review-seed', NOW(), NOW(), '微信审核占位商品', '0'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE merchant_id = 3 AND name = '【招牌】麻辣小龙虾（约1斤）'
);

INSERT INTO product (merchant_id, category_id, name, cover_image, main_image, price, original_price,
                     stock, sales, status, verify_type, valid_days, description, sort,
                     create_by, create_time, update_time, remark, del_flag)
SELECT 3, 7, '秘制烤肉串（20串）',
       '/profile/merchant_images/3/product/temp/main_421d2082.jpg',
       '/profile/merchant_images/3/product/temp/main_421d2082.jpg',
       45.00, 60.00, 200, 0, 1, 1, 30, '现穿现烤，孜然与辣椒可按口味调整。', 2,
       'review-seed', NOW(), NOW(), '微信审核占位商品', '0'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE merchant_id = 3 AND name = '秘制烤肉串（20串）'
);

INSERT INTO product (merchant_id, category_id, name, cover_image, main_image, price, original_price,
                     stock, sales, status, verify_type, valid_days, description, sort,
                     create_by, create_time, update_time, remark, del_flag)
SELECT 3, 7, '香辣烤鱼（2-3人份）',
       '/profile/merchant_images/3/product/temp/main_7458b1db.jpg',
       '/profile/merchant_images/3/product/temp/main_7458b1db.jpg',
       49.00, 68.00, 150, 0, 1, 1, 30, '现杀现烤，可另加配菜。', 3,
       'review-seed', NOW(), NOW(), '微信审核占位商品', '0'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE merchant_id = 3 AND name = '香辣烤鱼（2-3人份）'
);

INSERT INTO product (merchant_id, category_id, name, cover_image, main_image, price, original_price,
                     stock, sales, status, verify_type, valid_days, description, sort,
                     create_by, create_time, update_time, remark, del_flag)
SELECT 3, 7, '烧烤套餐（2人餐·含凉菜）',
       '/profile/merchant_images/3/product/temp/main_5fbe50e3.jpg',
       '/profile/merchant_images/3/product/temp/main_5fbe50e3.jpg',
       79.00, 128.00, 100, 0, 1, 1, 30, '烤肉串、烤鱼配凉菜拼盘，适合两人分享。', 4,
       'review-seed', NOW(), NOW(), '微信审核占位商品', '0'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE merchant_id = 3 AND name = '烧烤套餐（2人餐·含凉菜）'
);

INSERT INTO product (merchant_id, category_id, name, cover_image, main_image, price, original_price,
                     stock, sales, status, verify_type, valid_days, description, sort,
                     create_by, create_time, update_time, remark, del_flag)
SELECT 3, 7, '优质羊肉泡馍（单人份）',
       '/profile/merchant_images/3/product/temp/main_00d6c448.jpg',
       '/profile/merchant_images/3/product/temp/main_00d6c448.jpg',
       18.00, 22.00, 300, 0, 1, 1, 30, '羊肉汤现煮，馍现烙，可免费加汤。', 5,
       'review-seed', NOW(), NOW(), '微信审核占位商品', '0'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE merchant_id = 3 AND name = '优质羊肉泡馍（单人份）'
);

INSERT INTO product (merchant_id, category_id, name, cover_image, main_image, price, original_price,
                     stock, sales, status, verify_type, valid_days, description, sort,
                     create_by, create_time, update_time, remark, del_flag)
SELECT 3, 6, '冰峰汽水（200ml）',
       '/profile/merchant_images/3/product/temp/main_e4ea8941.jpg',
       '/profile/merchant_images/3/product/temp/main_e4ea8941.jpg',
       3.00, 4.00, 500, 0, 1, 1, 30, '经典橙味汽水，冰镇更好喝。', 6,
       'review-seed', NOW(), NOW(), '微信审核占位商品', '0'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM product WHERE merchant_id = 3 AND name = '冰峰汽水（200ml）'
);

-- 审核通过后清理占位商品（商品下架会同时让其自动生成的预点单服务失效）：
-- DELETE FROM product WHERE merchant_id = 3 AND remark = '微信审核占位商品';
