-- ============================================
-- 高标准测试数据：SaaS 多业态测试样本库
-- 数据库: ruoyi-cs
-- 前置: 01 + 02 + 03 已执行
-- 日期: 2026-05-28
-- ============================================

USE `ruoyi-cs`;

-- =============================================
-- 第一步：为现有 8 个商家补齐 AppID / Secret
-- =============================================

UPDATE merchant SET
    c_mini_app_id = 'wx_c000000000000005',
    c_mini_app_secret = 'test_c_secret_fruit_005xxxxxxxxxx',
    m_mini_app_id = 'wx_m000000000000005',
    m_mini_app_secret = 'test_m_secret_fruit_005xxxxxxxxxx',
    wx_pay_mch_id = '1600000005',
    wx_pay_api_key = 'test_apikey_fruit_005xxxxxxxxxx'
WHERE id = 5;

UPDATE merchant SET
    c_mini_app_id = 'wx_c000000000000006',
    c_mini_app_secret = 'test_c_secret_snack_006xxxxxxxxxx',
    m_mini_app_id = 'wx_m000000000000006',
    m_mini_app_secret = 'test_m_secret_snack_006xxxxxxxxxx',
    wx_pay_mch_id = '1600000006',
    wx_pay_api_key = 'test_apikey_snack_006xxxxxxxxxx'
WHERE id = 6;

UPDATE merchant SET
    c_mini_app_id = 'wx_c000000000000007',
    c_mini_app_secret = 'test_c_secret_fresh_007xxxxxxxxxx',
    m_mini_app_id = 'wx_m000000000000007',
    m_mini_app_secret = 'test_m_secret_fresh_007xxxxxxxxxx',
    wx_pay_mch_id = '1600000007',
    wx_pay_api_key = 'test_apikey_fresh_007xxxxxxxxxx'
WHERE id = 7;

UPDATE merchant SET
    c_mini_app_id = 'wx_c000000000000008',
    c_mini_app_secret = 'test_c_secret_tropi_008xxxxxxxxxx',
    m_mini_app_id = 'wx_m000000000000008',
    m_mini_app_secret = 'test_m_secret_tropi_008xxxxxxxxxx',
    wx_pay_mch_id = '1600000008',
    wx_pay_api_key = 'test_apikey_tropi_008xxxxxxxxxx'
WHERE id = 8;

UPDATE merchant SET
    c_mini_app_id = 'wx_c000000000000009',
    c_mini_app_secret = 'test_c_secret_canto_009xxxxxxxxxx',
    m_mini_app_id = 'wx_m000000000000009',
    m_mini_app_secret = 'test_m_secret_canto_009xxxxxxxxxx',
    wx_pay_mch_id = '1600000009',
    wx_pay_api_key = 'test_apikey_canto_009xxxxxxxxxx'
WHERE id = 9;

UPDATE merchant SET
    c_mini_app_id = 'wx_c000000000000010',
    c_mini_app_secret = 'test_c_secret_bjfd_010xxxxxxxxxxx',
    m_mini_app_id = 'wx_m000000000000010',
    m_mini_app_secret = 'test_m_secret_bjfd_010xxxxxxxxxxx',
    wx_pay_mch_id = '1600000010',
    wx_pay_api_key = 'test_apikey_bjfd_010xxxxxxxxxxx'
WHERE id = 10;

UPDATE merchant SET
    c_mini_app_id = 'wx_c000000000000011',
    c_mini_app_secret = 'test_c_secret_bfly_011xxxxxxxxxxx',
    m_mini_app_id = 'wx_m000000000000011',
    m_mini_app_secret = 'test_m_secret_bfly_011xxxxxxxxxxx',
    wx_pay_mch_id = '1600000011',
    wx_pay_api_key = 'test_apikey_bfly_011xxxxxxxxxxx'
WHERE id = 11;

UPDATE merchant SET
    c_mini_app_id = 'wx_c000000000000012',
    c_mini_app_secret = 'test_c_secret_yzsh_012xxxxxxxxxxx',
    m_mini_app_id = 'wx_m000000000000012',
    m_mini_app_secret = 'test_m_secret_yzsh_012xxxxxxxxxxx',
    wx_pay_mch_id = '1600000012',
    wx_pay_api_key = 'test_apikey_yzsh_012xxxxxxxxxxx'
WHERE id = 12;


-- =============================================
-- 第二步：新增多业态商家（15 个，覆盖 5 大行业 × 3 分销商）
-- =============================================

-- ===== 华东分销商 (dist=2) =====

-- 餐饮类（已有 5鲜果优选 6美味零食铺 7品质生鲜，新增：川渝火锅）
INSERT INTO merchant (id, name, contact, phone, distributor_id, status, balance, total_income, description, product_count, c_mini_app_id, c_mini_app_secret, m_mini_app_id, m_mini_app_secret, wx_pay_mch_id, wx_pay_api_key, del_flag, create_time)
VALUES (13, '蜀香火锅城', '赵经理', '13800001313', 2, 1, 15800.00, 52000.00, '正宗川渝火锅，新鲜食材当日配送', 0,
 'wx_c000000000000013', 'test_c_secret_hotpot013xxxxxxxx', 'wx_m000000000000013', 'test_m_secret_hotpot013xxxxxxxx', '1600000013', 'test_apikey_hotpot013xxxxxxxx', '0', NOW());

-- 景点/文旅
INSERT INTO merchant (id, name, contact, phone, distributor_id, status, balance, total_income, description, product_count, c_mini_app_id, c_mini_app_secret, m_mini_app_id, m_mini_app_secret, wx_pay_mch_id, wx_pay_api_key, del_flag, create_time)
VALUES (14, '东方明珠观光厅', '李经理', '13800001414', 2, 1, 28500.00, 96000.00, '上海地标建筑，360度俯瞰浦江两岸', 0,
 'wx_c000000000000014', 'test_c_secret_tower014xxxxxxxxx', 'wx_m000000000000014', 'test_m_secret_tower014xxxxxxxxx', '1600000014', 'test_apikey_tower014xxxxxxxxx', '0', NOW());

-- 酒店/住宿
INSERT INTO merchant (id, name, contact, phone, distributor_id, status, balance, total_income, description, product_count, c_mini_app_id, c_mini_app_secret, m_mini_app_id, m_mini_app_secret, wx_pay_mch_id, wx_pay_api_key, del_flag, create_time)
VALUES (15, '外滩精品民宿', '周经理', '13800001515', 2, 1, 12300.00, 41000.00, '外滩旁精品设计民宿，观陆家嘴夜景', 0,
 'wx_c000000000000015', 'test_c_secret_bnb015xxxxxxxxxxxx', 'wx_m000000000000015', 'test_m_secret_bnb015xxxxxxxxxxxx', '1600000015', 'test_apikey_bnb015xxxxxxxxxxxx', '0', NOW());

-- 演艺/文化
INSERT INTO merchant (id, name, contact, phone, distributor_id, status, balance, total_income, description, product_count, c_mini_app_id, c_mini_app_secret, m_mini_app_id, m_mini_app_secret, wx_pay_mch_id, wx_pay_api_key, del_flag, create_time)
VALUES (16, '大唐宫宴·上海站', '钱经理', '13800001616', 2, 1, 35000.00, 120000.00, '沉浸式唐风演艺盛宴，品宫廷宴席赏国风舞蹈', 0,
 'wx_c000000000000016', 'test_c_secret_tang016xxxxxxxxxx', 'wx_m000000000000016', 'test_m_secret_tang016xxxxxxxxxx', '1600000016', 'test_apikey_tang016xxxxxxxxxx', '0', NOW());

-- 娱乐/亲子
INSERT INTO merchant (id, name, contact, phone, distributor_id, status, balance, total_income, description, product_count, c_mini_app_id, c_mini_app_secret, m_mini_app_id, m_mini_app_secret, wx_pay_mch_id, wx_pay_api_key, del_flag, create_time)
VALUES (17, '欢乐谷亲子乐园', '吴经理', '13800001717', 2, 1, 42000.00, 150000.00, '华东地区大型亲子主题乐园，过山车+水世界+亲子剧场', 0,
 'wx_c000000000000017', 'test_c_secret_park017xxxxxxxxxx', 'wx_m000000000000017', 'test_m_secret_park017xxxxxxxxxx', '1600000017', 'test_apikey_park017xxxxxxxxxx', '0', NOW());


-- ===== 华南分销商 (dist=3) =====

-- 餐饮类
INSERT INTO merchant (id, name, contact, phone, distributor_id, status, balance, total_income, description, product_count, c_mini_app_id, c_mini_app_secret, m_mini_app_id, m_mini_app_secret, wx_pay_mch_id, wx_pay_api_key, del_flag, create_time)
VALUES (18, '点都德茶楼', '陈经理', '13800001818', 3, 1, 19800.00, 65000.00, '广州老字号广式茶楼，传承经典粤式点心', 0,
 'wx_c000000000000018', 'test_c_secret_teaa018xxxxxxxxx', 'wx_m000000000000018', 'test_m_secret_teaa018xxxxxxxxx', '1600000018', 'test_apikey_teaa018xxxxxxxxx', '0', NOW());

-- 景点/文旅
INSERT INTO merchant (id, name, contact, phone, distributor_id, status, balance, total_income, description, product_count, c_mini_app_id, c_mini_app_secret, m_mini_app_id, m_mini_app_secret, wx_pay_mch_id, wx_pay_api_key, del_flag, create_time)
VALUES (19, '长隆野生动物世界', '林经理', '13800001919', 3, 1, 56000.00, 198000.00, '亚洲最大野生动物主题公园，500余种珍稀动物', 0,
 'wx_c000000000000019', 'test_c_secret_safa019xxxxxxxxx', 'wx_m000000000000019', 'test_m_secret_safa019xxxxxxxxx', '1600000019', 'test_apikey_safa019xxxxxxxxx', '0', NOW());

-- 酒店/住宿
INSERT INTO merchant (id, name, contact, phone, distributor_id, status, balance, total_income, description, product_count, c_mini_app_id, c_mini_app_secret, m_mini_app_id, m_mini_app_secret, wx_pay_mch_id, wx_pay_api_key, del_flag, create_time)
VALUES (20, '珠海长隆企鹅酒店', '黄经理', '13800002020', 3, 1, 38000.00, 135000.00, '海洋王国旁亲子主题酒店，企鹅陪你入眠', 0,
 'wx_c000000000000020', 'test_c_secret_peng020xxxxxxxxx', 'wx_m000000000000020', 'test_m_secret_peng020xxxxxxxxx', '1600000020', 'test_apikey_peng020xxxxxxxxx', '0', NOW());

-- 演艺/文化
INSERT INTO merchant (id, name, contact, phone, distributor_id, status, balance, total_income, description, product_count, c_mini_app_id, c_mini_app_secret, m_mini_app_id, m_mini_app_secret, wx_pay_mch_id, wx_pay_api_key, del_flag, create_time)
VALUES (21, '广州大剧院', '郑经理', '13800002121', 3, 1, 22000.00, 78000.00, '华南地区顶级演艺场馆，歌剧话剧音乐会', 0,
 'wx_c000000000000021', 'test_c_secret_opera021xxxxxxxx', 'wx_m000000000000021', 'test_m_secret_opera021xxxxxxxx', '1600000021', 'test_apikey_opera021xxxxxxxx', '0', NOW());

-- 娱乐/亲子
INSERT INTO merchant (id, name, contact, phone, distributor_id, status, balance, total_income, description, product_count, c_mini_app_id, c_mini_app_secret, m_mini_app_id, m_mini_app_secret, wx_pay_mch_id, wx_pay_api_key, del_flag, create_time)
VALUES (22, '密室逃脱·广州旗舰店', '冯经理', '13800002222', 3, 1, 8500.00, 29000.00, '沉浸式密室逃脱+剧本杀，多主题多难度', 0,
 'wx_c000000000000022', 'test_c_secret_room022xxxxxxxxx', 'wx_m000000000000022', 'test_m_secret_room022xxxxxxxxx', '1600000022', 'test_apikey_room022xxxxxxxxx', '0', NOW());


-- ===== 华北分销商 (dist=4) =====

-- 餐饮类
INSERT INTO merchant (id, name, contact, phone, distributor_id, status, balance, total_income, description, product_count, c_mini_app_id, c_mini_app_secret, m_mini_app_id, m_mini_app_secret, wx_pay_mch_id, wx_pay_api_key, del_flag, create_time)
VALUES (23, '全聚德烤鸭店', '孙经理', '13800002323', 4, 1, 21000.00, 72000.00, '百年老字号，正宗挂炉烤鸭', 0,
 'wx_c000000000000023', 'test_c_secret_duck023xxxxxxxxx', 'wx_m000000000000023', 'test_m_secret_duck023xxxxxxxxx', '1600000023', 'test_apikey_duck023xxxxxxxxx', '0', NOW());

-- 景点/文旅
INSERT INTO merchant (id, name, contact, phone, distributor_id, status, balance, total_income, description, product_count, c_mini_app_id, c_mini_app_secret, m_mini_app_id, m_mini_app_secret, wx_pay_mch_id, wx_pay_api_key, del_flag, create_time)
VALUES (24, '故宫博物院文创票务', '马经理', '13800002424', 4, 1, 68000.00, 230000.00, '世界文化遗产，联票+钟表馆+珍宝馆', 0,
 'wx_c000000000000024', 'test_c_secret_gugong024xxxxxxxx', 'wx_m000000000000024', 'test_m_secret_gugong024xxxxxxxx', '1600000024', 'test_apikey_gugong024xxxxxxxx', '0', NOW());

-- 酒店/住宿
INSERT INTO merchant (id, name, contact, phone, distributor_id, status, balance, total_income, description, product_count, c_mini_app_id, c_mini_app_secret, m_mini_app_id, m_mini_app_secret, wx_pay_mch_id, wx_pay_api_key, del_flag, create_time)
VALUES (25, '北京胡同四合院民宿', '朱经理', '13800002525', 4, 1, 9800.00, 33000.00, '老北京胡同深处精品四合院，体验京味儿生活', 0,
 'wx_c000000000000025', 'test_c_secret_hutong024xxxxxxx', 'wx_m000000000000025', 'test_m_secret_hutong024xxxxxxx', '1600000025', 'test_apikey_hutong024xxxxxxx', '0', NOW());

-- 演艺/文化
INSERT INTO merchant (id, name, contact, phone, distributor_id, status, balance, total_income, description, product_count, c_mini_app_id, c_mini_app_secret, m_mini_app_id, m_mini_app_secret, wx_pay_mch_id, wx_pay_api_key, del_flag, create_time)
VALUES (26, '德云社·三里屯剧场', '杨经理', '13800002626', 4, 1, 16500.00, 55000.00, '传统相声+评书，京城文化名片', 0,
 'wx_c000000000000026', 'test_c_secret_deyun026xxxxxxxx', 'wx_m000000000000026', 'test_m_secret_deyun026xxxxxxxx', '1600000026', 'test_apikey_deyun026xxxxxxxx', '0', NOW());

-- 娱乐/亲子
INSERT INTO merchant (id, name, contact, phone, distributor_id, status, balance, total_income, description, product_count, c_mini_app_id, c_mini_app_secret, m_mini_app_id, m_mini_app_secret, wx_pay_mch_id, wx_pay_api_key, del_flag, create_time)
VALUES (27, '万达影城·CBD店', '高经理', '13800002727', 4, 1, 11200.00, 38000.00, 'IMAX+杜比全景声，CBD核心商圈旗舰影城', 0,
 'wx_c000000000000027', 'test_c_secret_cine027xxxxxxxxx', 'wx_m000000000000027', 'test_m_secret_cine027xxxxxxxxx', '1600000027', 'test_apikey_cine027xxxxxxxxx', '0', NOW());


-- =============================================
-- 第三步：为新商家补充门店
-- =============================================

-- 华东
INSERT INTO merchant_store (id, merchant_id, name, address, phone, longitude, latitude, status, create_time) VALUES
(5, 13, '蜀香火锅城(南京东路旗舰店)', '上海市黄浦区南京东路128号', '021-66661313', 121.4800, 31.2400, 1, NOW()),
(6, 13, '蜀香火锅城(浦东店)', '上海市浦东新区陆家嘴环路100号', '021-66661314', 121.5050, 31.2400, 1, NOW()),
(7, 14, '东方明珠观光厅(主塔)', '上海市浦东新区世纪大道1号', '021-58791888', 121.5060, 31.2400, 1, NOW()),
(8, 15, '外滩精品民宿(南京路店)', '上海市黄浦区中山东一路28号', '021-63230000', 121.4900, 31.2400, 1, NOW()),
(9, 15, '外滩精品民宿(豫园店)', '上海市黄浦区安仁街58号', '021-63230001', 121.4910, 31.2300, 1, NOW()),
(10, 16, '大唐宫宴·上海(世博源店)', '上海市浦东新区世博大道1368号', '021-20260016', 121.4900, 31.1800, 1, NOW()),
(11, 17, '欢乐谷亲子乐园(上海)', '上海市松江区林湖路888号', '021-33552222', 121.2200, 31.0700, 1, NOW()),
(12, 17, '欢乐谷亲子乐园(嘉定水世界)', '上海市嘉定区伊宁路2000号', '021-33552223', 121.2600, 31.3700, 1, NOW());

-- 华南
INSERT INTO merchant_store (id, merchant_id, name, address, phone, longitude, latitude, status, create_time) VALUES
(13, 18, '点都德茶楼(天河城店)', '广州市天河区天河路208号', '020-85551818', 113.3300, 23.1300, 1, NOW()),
(14, 18, '点都德茶楼(上下九店)', '广州市荔湾区第十甫路62号', '020-81391818', 113.2500, 23.1200, 1, NOW()),
(15, 19, '长隆野生动物世界(南门)', '广州市番禺区大石镇105国道', '020-84783333', 113.3200, 23.0000, 1, NOW()),
(16, 20, '珠海长隆企鹅酒店', '珠海市横琴新区富祥湾', '0756-2998888', 113.5400, 22.1300, 1, NOW()),
(17, 21, '广州大剧院(歌剧厅)', '广州市天河区珠江新城珠江西路1号', '020-38392888', 113.3200, 23.1200, 1, NOW()),
(18, 21, '广州大剧院(实验剧场)', '广州市天河区珠江新城珠江西路1号', '020-38392889', 113.3200, 23.1200, 1, NOW()),
(19, 22, '密室逃脱(天河旗舰店)', '广州市天河区体育西路123号', '020-38882222', 113.3200, 23.1400, 1, NOW());

-- 华北
INSERT INTO merchant_store (id, merchant_id, name, address, phone, longitude, latitude, status, create_time) VALUES
(20, 23, '全聚德烤鸭(前门店)', '北京市东城区前门大街30号', '010-65112418', 116.4000, 39.9000, 1, NOW()),
(21, 23, '全聚德烤鸭(王府井店)', '北京市东城区王府井大街帅府园胡同9号', '010-65228384', 116.4100, 39.9200, 1, NOW()),
(22, 24, '故宫博物院(午门入口)', '北京市东城区景山前街4号', '010-85007421', 116.4000, 39.9200, 1, NOW()),
(23, 25, '胡同四合院民宿(南锣鼓巷店)', '北京市东城区南锣鼓巷88号', '010-64010000', 116.4050, 39.9400, 1, NOW()),
(24, 26, '德云社·三里屯剧场', '北京市朝阳区三里屯路19号', '010-64177777', 116.4500, 39.9300, 1, NOW()),
(25, 27, '万达影城(CBD店)', '北京市朝阳区建国路93号院', '010-85852727', 116.4700, 39.9100, 1, NOW());


-- =============================================
-- 第四步：为新商家补充商品分类
-- =============================================

-- 蜀香火锅城 (id=13)
INSERT INTO product_category (id, merchant_id, name, sort, create_time) VALUES
(10, 13, '锅底', 1, NOW()),
(11, 13, '涮菜肉类', 2, NOW()),
(12, 13, '涮菜蔬菜', 3, NOW()),
(13, 13, '火锅套餐', 4, NOW());

-- 东方明珠 (id=14)
INSERT INTO product_category (id, merchant_id, name, sort, create_time) VALUES
(14, 14, '观光门票', 1, NOW()),
(15, 14, '套票', 2, NOW());

-- 外滩民宿 (id=15)
INSERT INTO product_category (id, merchant_id, name, sort, create_time) VALUES
(16, 15, '客房套餐', 1, NOW()),
(17, 15, '体验活动', 2, NOW());

-- 大唐宫宴 (id=16)
INSERT INTO product_category (id, merchant_id, name, sort, create_time) VALUES
(18, 16, '演出门票', 1, NOW()),
(19, 16, '宴席套餐', 2, NOW());

-- 欢乐谷 (id=17)
INSERT INTO product_category (id, merchant_id, name, sort, create_time) VALUES
(20, 17, '门票', 1, NOW()),
(21, 17, '年卡/季卡', 2, NOW()),
(22, 17, '亲子套票', 3, NOW());

-- 点都德 (id=18)
INSERT INTO product_category (id, merchant_id, name, sort, create_time) VALUES
(23, 18, '早茶套餐', 1, NOW()),
(24, 18, '经典点心', 2, NOW()),
(25, 18, '饮品甜品', 3, NOW());

-- 长隆动物园 (id=19)
INSERT INTO product_category (id, merchant_id, name, sort, create_time) VALUES
(26, 19, '门票', 1, NOW()),
(27, 19, '家庭套票', 2, NOW());

-- 企鹅酒店 (id=20)
INSERT INTO product_category (id, merchant_id, name, sort, create_time) VALUES
(28, 20, '客房套餐', 1, NOW()),
(29, 20, '酒店+乐园套票', 2, NOW());

-- 广州大剧院 (id=21)
INSERT INTO product_category (id, merchant_id, name, sort, create_time) VALUES
(30, 21, '演出票', 1, NOW()),
(31, 21, 'VIP套票', 2, NOW());

-- 密室逃脱 (id=22)
INSERT INTO product_category (id, merchant_id, name, sort, create_time) VALUES
(32, 22, '密室主题', 1, NOW()),
(33, 22, '剧本杀', 2, NOW());

-- 全聚德 (id=23)
INSERT INTO product_category (id, merchant_id, name, sort, create_time) VALUES
(34, 23, '烤鸭套餐', 1, NOW()),
(35, 23, '京味菜', 2, NOW()),
(36, 23, '商务宴请', 3, NOW());

-- 故宫 (id=24)
INSERT INTO product_category (id, merchant_id, name, sort, create_time) VALUES
(37, 24, '门票', 1, NOW()),
(38, 24, '讲解服务', 2, NOW()),
(39, 24, '文创套餐', 3, NOW());

-- 胡同民宿 (id=25)
INSERT INTO product_category (id, merchant_id, name, sort, create_time) VALUES
(40, 25, '客房', 1, NOW()),
(41, 25, '胡同体验', 2, NOW());

-- 德云社 (id=26)
INSERT INTO product_category (id, merchant_id, name, sort, create_time) VALUES
(42, 26, '演出票', 1, NOW()),
(43, 26, '茶座套餐', 2, NOW());

-- 万达影城 (id=27)
INSERT INTO product_category (id, merchant_id, name, sort, create_time) VALUES
(44, 27, '电影票', 1, NOW()),
(45, 27, '套餐', 2, NOW());


-- =============================================
-- 第五步：为新商家补充商品
-- =============================================

-- 蜀香火锅城 (merchant=13)
INSERT INTO product (id, merchant_id, category_id, name, cover_image, images, price, original_price, stock, sales, status, description, valid_days, sort, create_time) VALUES
(18, 13, 10, '经典牛油锅底', '/profile/merchant_images/13/product/cover_18.jpg', NULL, 38.00, 48.00, 999, 0, 1, '秘制牛油底料，麻辣鲜香', 30, 1, NOW()),
(19, 13, 11, '精品肥牛卷(半斤)', '/profile/merchant_images/13/product/cover_19.jpg', NULL, 42.00, 52.00, 200, 0, 1, '精选澳洲肥牛，雪花纹路清晰', 30, 2, NOW()),
(20, 13, 12, '时蔬拼盘', '/profile/merchant_images/13/product/cover_20.jpg', NULL, 18.00, 25.00, 500, 0, 1, '新鲜时令蔬菜6种拼盘', 30, 3, NOW()),
(21, 13, 13, '双人火锅套餐', '/profile/merchant_images/13/product/cover_21.jpg', NULL, 158.00, 218.00, 100, 0, 1, '锅底+肥牛+毛肚+蔬菜拼盘+面条，两人够吃', 30, 4, NOW()),
(22, 13, 13, '四人豪华套餐', '/profile/merchant_images/13/product/cover_22.jpg', NULL, 298.00, 428.00, 50, 0, 1, '鸳鸯锅底+鲜切牛肉+羔羊肉+毛肚+虾滑+蔬菜拼盘+主食', 30, 5, NOW());

-- 东方明珠 (merchant=14)
INSERT INTO product (id, merchant_id, category_id, name, cover_image, images, price, original_price, stock, sales, status, description, valid_days, sort, create_time) VALUES
(23, 14, 14, '二球联票(成人)', '/profile/merchant_images/14/product/cover_23.jpg', NULL, 120.00, 160.00, 5000, 0, 1, '263米主观光层+259米全透明悬空观光廊', 7, 1, NOW()),
(24, 14, 14, '三球联票(成人)', '/profile/merchant_images/14/product/cover_24.jpg', NULL, 180.00, 220.00, 3000, 0, 1, '含351米太空舱+二球全部内容', 7, 2, NOW()),
(25, 14, 14, '儿童票(1.0-1.4m)', '/profile/merchant_images/14/product/cover_25.jpg', NULL, 60.00, 80.00, 3000, 0, 1, '身高1.0-1.4米儿童适用', 7, 3, NOW()),
(26, 14, 15, '亲子套票(1大1小)', '/profile/merchant_images/14/product/cover_26.jpg', NULL, 168.00, 240.00, 2000, 0, 1, '二球联票+旋转餐厅下午茶', 7, 4, NOW());

-- 外滩民宿 (merchant=15)
INSERT INTO product (id, merchant_id, category_id, name, cover_image, images, price, original_price, stock, sales, status, description, valid_days, sort, create_time) VALUES
(27, 15, 16, '江景大床房(含早)', '/profile/merchant_images/15/product/cover_27.jpg', NULL, 598.00, 888.00, 10, 0, 1, '270度江景落地窗，含双人早餐+下午茶', 90, 1, NOW()),
(28, 15, 16, '外滩套房(含早+晚宴)', '/profile/merchant_images/15/product/cover_28.jpg', NULL, 1280.00, 1888.00, 5, 0, 1, '60平套房，正对陆家嘴，含双早+烛光晚宴', 90, 2, NOW()),
(29, 15, 17, '上海老弄堂文化漫步(2小时)', '/profile/merchant_images/15/product/cover_29.jpg', NULL, 98.00, 158.00, 50, 0, 1, '专业导游带你走老上海弄堂，了解百年历史', 30, 3, NOW());

-- 大唐宫宴 (merchant=16)
INSERT INTO product (id, merchant_id, category_id, name, cover_image, images, price, original_price, stock, sales, status, description, valid_days, sort, create_time) VALUES
(30, 16, 18, '普通席演出票(成人)', '/profile/merchant_images/16/product/cover_30.jpg', NULL, 268.00, 368.00, 300, 0, 1, '沉浸式唐风演艺+歌舞+杂技，演出90分钟', 30, 1, NOW()),
(31, 16, 18, 'VIP席演出票(成人)', '/profile/merchant_images/16/product/cover_31.jpg', NULL, 468.00, 668.00, 100, 0, 1, '前排VIP席位，含演员互动环节', 30, 2, NOW()),
(32, 16, 19, '双人宫宴套餐', '/profile/merchant_images/16/product/cover_32.jpg', NULL, 688.00, 998.00, 80, 0, 1, 'VIP席×2+唐风宫廷宴席(8道菜)', 30, 3, NOW()),
(33, 16, 18, '亲子票(1大1小)', '/profile/merchant_images/16/product/cover_33.jpg', NULL, 328.00, 448.00, 200, 0, 1, '含儿童汉服体验+演出观赏', 30, 4, NOW());

-- 欢乐谷 (merchant=17)
INSERT INTO product (id, merchant_id, category_id, name, cover_image, images, price, original_price, stock, sales, status, description, valid_days, sort, create_time) VALUES
(34, 17, 20, '全日票(成人)', '/profile/merchant_images/17/product/cover_34.jpg', NULL, 230.00, 299.00, 5000, 0, 1, '包含所有游乐设施，不含水世界', 7, 1, NOW()),
(35, 17, 20, '夜场票(成人)', '/profile/merchant_images/17/product/cover_35.jpg', NULL, 99.00, 150.00, 3000, 0, 1, '17:00-22:00夜场，含夜光巡游', 7, 2, NOW()),
(36, 17, 22, '亲子套票(1大1小)', '/profile/merchant_images/17/product/cover_36.jpg', NULL, 298.00, 420.00, 2000, 0, 1, '全日票+儿童1.4m以下免票+亲子项目4选2', 7, 3, NOW()),
(37, 17, 21, '年卡(成人)', '/profile/merchant_images/17/product/cover_37.jpg', NULL, 599.00, 888.00, 500, 0, 1, '全年无限次入园，含节假日', 365, 4, NOW()),
(38, 17, 22, '家庭票(2大1小)', '/profile/merchant_images/17/product/cover_38.jpg', NULL, 498.00, 720.00, 1500, 0, 1, '两大一小全日票+亲子午餐套餐', 7, 5, NOW());

-- 点都德 (merchant=18)
INSERT INTO product (id, merchant_id, category_id, name, cover_image, images, price, original_price, stock, sales, status, description, valid_days, sort, create_time) VALUES
(39, 18, 23, '经典早茶双人套餐', '/profile/merchant_images/18/product/cover_39.jpg', NULL, 128.00, 186.00, 200, 0, 1, '虾饺+烧卖+叉烧包+肠粉+皮蛋粥+茶位×2', 14, 1, NOW()),
(40, 18, 24, '水晶虾饺皇(4只)', '/profile/merchant_images/18/product/cover_40.jpg', NULL, 32.00, 38.00, 500, 0, 1, '皮薄馅大，鲜虾看得见', 14, 2, NOW()),
(41, 18, 24, '蟹子烧卖皇(4只)', '/profile/merchant_images/18/product/cover_41.jpg', NULL, 28.00, 35.00, 500, 0, 1, '蟹子+虾仁+猪肉经典三件套', 14, 3, NOW()),
(42, 18, 23, '四人家庭早茶', '/profile/merchant_images/18/product/cover_42.jpg', NULL, 238.00, 338.00, 100, 0, 1, '10款经典点心+4份粥粉面+茶位×4', 14, 4, NOW());

-- 长隆动物园 (merchant=19)
INSERT INTO product (id, merchant_id, category_id, name, cover_image, images, price, original_price, stock, sales, status, description, valid_days, sort, create_time) VALUES
(43, 19, 26, '成人票', '/profile/merchant_images/19/product/cover_43.jpg', NULL, 300.00, 350.00, 10000, 0, 1, '含自驾区+步行区+小火车', 7, 1, NOW()),
(44, 19, 26, '儿童票(1.0-1.5m)', '/profile/merchant_images/19/product/cover_44.jpg', NULL, 210.00, 245.00, 8000, 0, 1, '身高1.0-1.5m儿童适用', 7, 2, NOW()),
(45, 19, 27, '家庭套票(2大1小)', '/profile/merchant_images/19/product/cover_45.jpg', NULL, 720.00, 895.00, 5000, 0, 1, '两大一小+园内午餐套餐', 7, 3, NOW());

-- 企鹅酒店 (merchant=20)
INSERT INTO product (id, merchant_id, category_id, name, cover_image, images, price, original_price, stock, sales, status, description, valid_days, sort, create_time) VALUES
(46, 20, 28, '极地房(含双早)', '/profile/merchant_images/20/product/cover_46.jpg', NULL, 888.00, 1288.00, 30, 0, 1, '企鹅主题房，窗外可看企鹅，含双早', 90, 1, NOW()),
(47, 20, 29, '酒店+海洋王国2日套票(2大1小)', '/profile/merchant_images/20/product/cover_47.jpg', NULL, 1988.00, 2688.00, 20, 0, 1, '极地房1晚+海洋王国2日无限次入园+双早', 90, 2, NOW());

-- 广州大剧院 (merchant=21)
INSERT INTO product (id, merchant_id, category_id, name, cover_image, images, price, original_price, stock, sales, status, description, valid_days, sort, create_time) VALUES
(48, 21, 30, '歌剧《图兰朵》A区票', '/profile/merchant_images/21/product/cover_48.jpg', NULL, 680.00, 880.00, 200, 0, 1, '经典普契尼歌剧，意大利语演唱中文字幕', 30, 1, NOW()),
(49, 21, 30, '歌剧《图兰朵》B区票', '/profile/merchant_images/21/product/cover_49.jpg', NULL, 380.00, 480.00, 400, 0, 1, '同上演出，B区观演位置', 30, 2, NOW()),
(50, 21, 31, 'VIP双人套票(A区+晚宴)', '/profile/merchant_images/21/product/cover_50.jpg', NULL, 1580.00, 2180.00, 50, 0, 1, 'A区×2+演出前西式晚宴+节目册', 30, 3, NOW());

-- 密室逃脱 (merchant=22)
INSERT INTO product (id, merchant_id, category_id, name, cover_image, images, price, original_price, stock, sales, status, description, valid_days, sort, create_time) VALUES
(51, 22, 32, '恐怖密室·医院(4-6人)', '/profile/merchant_images/22/product/cover_51.jpg', NULL, 128.00, 168.00, 80, 0, 1, '90分钟恐怖主题密室，NPC真人互动', 30, 1, NOW()),
(52, 22, 32, '科幻密室·太空站(4-8人)', '/profile/merchant_images/22/product/cover_52.jpg', NULL, 148.00, 198.00, 80, 0, 1, '机关重重的太空逃生，适合团队', 30, 2, NOW()),
(53, 22, 33, '古风剧本杀·长安迷案(6-8人)', '/profile/merchant_images/22/product/cover_53.jpg', NULL, 168.00, 228.00, 60, 0, 1, '唐风悬疑推理，换装+DM全程带本', 30, 3, NOW());

-- 全聚德 (merchant=23)
INSERT INTO product (id, merchant_id, category_id, name, cover_image, images, price, original_price, stock, sales, status, description, valid_days, sort, create_time) VALUES
(54, 23, 34, '招牌烤鸭半只', '/profile/merchant_images/23/product/cover_54.jpg', NULL, 158.00, 198.00, 100, 0, 1, '挂炉果木烤鸭，现烤现片，半只约50片', 14, 1, NOW()),
(55, 23, 34, '烤鸭整只(含饼葱酱)', '/profile/merchant_images/23/product/cover_55.jpg', NULL, 298.00, 368.00, 80, 0, 1, '整只烤鸭+荷叶饼+葱丝+甜面酱', 14, 2, NOW()),
(56, 23, 35, '京味四小碟', '/profile/merchant_images/23/product/cover_56.jpg', NULL, 68.00, 88.00, 200, 0, 1, '芥末墩+乾隆白菜+酥鱼+拌豆丝', 14, 3, NOW()),
(57, 23, 36, '商务宴请套餐(6人)', '/profile/merchant_images/23/product/cover_57.jpg', NULL, 1280.00, 1680.00, 30, 0, 1, '整只烤鸭+6热菜+2凉菜+主食+水果拼盘', 14, 4, NOW());

-- 故宫 (merchant=24)
INSERT INTO product (id, merchant_id, category_id, name, cover_image, images, price, original_price, stock, sales, status, description, valid_days, sort, create_time) VALUES
(58, 24, 37, '故宫门票(旺季成人)', '/profile/merchant_images/24/product/cover_58.jpg', NULL, 60.00, 60.00, 80000, 0, 1, '故宫博物院参观门票，需预约', 1, 1, NOW()),
(59, 24, 37, '珍宝馆门票', '/profile/merchant_images/24/product/cover_59.jpg', NULL, 10.00, 10.00, 50000, 0, 1, '宁寿宫区珍宝馆附加门票', 1, 2, NOW()),
(60, 24, 38, '故宫深度讲解(3小时)', '/profile/merchant_images/24/product/cover_60.jpg', NULL, 198.00, 298.00, 500, 0, 1, '专业文史导游，中轴线+东西六宫精讲', 7, 3, NOW()),
(61, 24, 39, '故宫文创福袋', '/profile/merchant_images/24/product/cover_61.jpg', NULL, 168.00, 228.00, 1000, 0, 1, '含故宫日历+书签+冰箱贴+手机支架', 30, 4, NOW());

-- 胡同民宿 (merchant=25)
INSERT INTO product (id, merchant_id, category_id, name, cover_image, images, price, original_price, stock, sales, status, description, valid_days, sort, create_time) VALUES
(62, 25, 40, '标准间(含早)', '/profile/merchant_images/25/product/cover_62.jpg', NULL, 458.00, 688.00, 8, 0, 1, '正宗四合院标准间，含老北京早点', 90, 1, NOW()),
(63, 25, 40, '正房大套房(含早+下午茶)', '/profile/merchant_images/25/product/cover_63.jpg', NULL, 888.00, 1288.00, 4, 0, 1, '正房30平套间，传统中式家具，含下午茶', 90, 2, NOW()),
(64, 25, 41, '胡同人力车游(2小时)', '/profile/merchant_images/25/product/cover_64.jpg', NULL, 128.00, 188.00, 30, 0, 1, '坐老北京人力车逛胡同+茶馆听戏', 30, 3, NOW());

-- 德云社 (merchant=26)
INSERT INTO product (id, merchant_id, category_id, name, cover_image, images, price, original_price, stock, sales, status, description, valid_days, sort, create_time) VALUES
(65, 26, 42, '晚场相声票(散座)', '/profile/merchant_images/26/product/cover_65.jpg', NULL, 100.00, 120.00, 200, 0, 1, '19:30开场，约6组相声+1组压轴', 7, 1, NOW()),
(66, 26, 42, '晚场相声票(包厢)', '/profile/merchant_images/26/product/cover_66.jpg', NULL, 200.00, 280.00, 80, 0, 1, '二楼包厢座，视野好空间私密', 7, 2, NOW()),
(67, 26, 43, '茶座套餐(含瓜子花生)', '/profile/merchant_images/26/product/cover_67.jpg', NULL, 58.00, 88.00, 300, 0, 1, '盖碗茶+瓜子+花生+蜜饯果盘', 7, 3, NOW());

-- 万达影城 (merchant=27)
INSERT INTO product (id, merchant_id, category_id, name, cover_image, images, price, original_price, stock, sales, status, description, valid_days, sort, create_time) VALUES
(68, 27, 44, 'IMAX厅电影票(成人)', '/profile/merchant_images/27/product/cover_68.jpg', NULL, 68.00, 88.00, 500, 0, 1, 'IMAX巨幕厅普通场次', 7, 1, NOW()),
(69, 27, 44, '杜比全景声厅电影票', '/profile/merchant_images/27/product/cover_69.jpg', NULL, 55.00, 75.00, 500, 0, 1, '杜比全景声影厅，极致视听体验', 7, 2, NOW()),
(70, 27, 45, '双人观影套餐(2张票+爆米花+可乐×2)', '/profile/merchant_images/27/product/cover_70.jpg', NULL, 128.00, 178.00, 300, 0, 1, '普通厅×2+大桶爆米花+大杯可乐×2', 7, 3, NOW());


-- =============================================
-- 第六步：补齐 product_image 表数据（关键链路）
-- =============================================

-- 商家 13 蜀香火锅城
INSERT INTO product_image (product_id, merchant_id, image_type, image_url, sort_order, status, create_time) VALUES
(18, 13, 'main', '/profile/merchant_images/13/product/main_18_01.jpg', 1, 1, NOW()),
(18, 13, 'detail', '/profile/merchant_images/13/product/detail_18_01.jpg', 2, 1, NOW()),
(19, 13, 'main', '/profile/merchant_images/13/product/main_19_01.jpg', 1, 1, NOW()),
(21, 13, 'main', '/profile/merchant_images/13/product/main_21_01.jpg', 1, 1, NOW()),
(21, 13, 'detail', '/profile/merchant_images/13/product/detail_21_01.jpg', 2, 1, NOW()),
(21, 13, 'detail', '/profile/merchant_images/13/product/detail_21_02.jpg', 3, 1, NOW());

-- 商家 14 东方明珠
INSERT INTO product_image (product_id, merchant_id, image_type, image_url, sort_order, status, create_time) VALUES
(23, 14, 'main', '/profile/merchant_images/14/product/main_23_01.jpg', 1, 1, NOW()),
(23, 14, 'detail', '/profile/merchant_images/14/product/detail_23_01.jpg', 2, 1, NOW()),
(23, 14, 'detail', '/profile/merchant_images/14/product/detail_23_02.jpg', 3, 1, NOW()),
(24, 14, 'main', '/profile/merchant_images/14/product/main_24_01.jpg', 1, 1, NOW()),
(26, 14, 'main', '/profile/merchant_images/14/product/main_26_01.jpg', 1, 1, NOW());

-- 商家 15 外滩民宿
INSERT INTO product_image (product_id, merchant_id, image_type, image_url, sort_order, status, create_time) VALUES
(27, 15, 'main', '/profile/merchant_images/15/product/main_27_01.jpg', 1, 1, NOW()),
(27, 15, 'detail', '/profile/merchant_images/15/product/detail_27_01.jpg', 2, 1, NOW()),
(27, 15, 'detail', '/profile/merchant_images/15/product/detail_27_02.jpg', 3, 1, NOW()),
(27, 15, 'detail', '/profile/merchant_images/15/product/detail_27_03.jpg', 4, 1, NOW()),
(28, 15, 'main', '/profile/merchant_images/15/product/main_28_01.jpg', 1, 1, NOW());

-- 商家 16 大唐宫宴
INSERT INTO product_image (product_id, merchant_id, image_type, image_url, sort_order, status, create_time) VALUES
(30, 16, 'main', '/profile/merchant_images/16/product/main_30_01.jpg', 1, 1, NOW()),
(30, 16, 'detail', '/profile/merchant_images/16/product/detail_30_01.jpg', 2, 1, NOW()),
(31, 16, 'main', '/profile/merchant_images/16/product/main_31_01.jpg', 1, 1, NOW()),
(32, 16, 'main', '/profile/merchant_images/16/product/main_32_01.jpg', 1, 1, NOW()),
(32, 16, 'detail', '/profile/merchant_images/16/product/detail_32_01.jpg', 2, 1, NOW()),
(32, 16, 'detail', '/profile/merchant_images/16/product/detail_32_02.jpg', 3, 1, NOW());

-- 商家 17 欢乐谷
INSERT INTO product_image (product_id, merchant_id, image_type, image_url, sort_order, status, create_time) VALUES
(34, 17, 'main', '/profile/merchant_images/17/product/main_34_01.jpg', 1, 1, NOW()),
(34, 17, 'detail', '/profile/merchant_images/17/product/detail_34_01.jpg', 2, 1, NOW()),
(36, 17, 'main', '/profile/merchant_images/17/product/main_36_01.jpg', 1, 1, NOW()),
(37, 17, 'main', '/profile/merchant_images/17/product/main_37_01.jpg', 1, 1, NOW()),
(38, 17, 'main', '/profile/merchant_images/17/product/main_38_01.jpg', 1, 1, NOW());

-- 商家 18 点都德
INSERT INTO product_image (product_id, merchant_id, image_type, image_url, sort_order, status, create_time) VALUES
(39, 18, 'main', '/profile/merchant_images/18/product/main_39_01.jpg', 1, 1, NOW()),
(39, 18, 'detail', '/profile/merchant_images/18/product/detail_39_01.jpg', 2, 1, NOW()),
(40, 18, 'main', '/profile/merchant_images/18/product/main_40_01.jpg', 1, 1, NOW()),
(42, 18, 'main', '/profile/merchant_images/18/product/main_42_01.jpg', 1, 1, NOW());

-- 其他商家各至少 1 张主图
INSERT INTO product_image (product_id, merchant_id, image_type, image_url, sort_order, status, create_time) VALUES
(43, 19, 'main', '/profile/merchant_images/19/product/main_43_01.jpg', 1, 1, NOW()),
(45, 19, 'main', '/profile/merchant_images/19/product/main_45_01.jpg', 1, 1, NOW()),
(46, 20, 'main', '/profile/merchant_images/20/product/main_46_01.jpg', 1, 1, NOW()),
(47, 20, 'main', '/profile/merchant_images/20/product/main_47_01.jpg', 1, 1, NOW()),
(48, 21, 'main', '/profile/merchant_images/21/product/main_48_01.jpg', 1, 1, NOW()),
(48, 21, 'detail', '/profile/merchant_images/21/product/detail_48_01.jpg', 2, 1, NOW()),
(51, 22, 'main', '/profile/merchant_images/22/product/main_51_01.jpg', 1, 1, NOW()),
(52, 22, 'main', '/profile/merchant_images/22/product/main_52_01.jpg', 1, 1, NOW()),
(54, 23, 'main', '/profile/merchant_images/23/product/main_54_01.jpg', 1, 1, NOW()),
(55, 23, 'main', '/profile/merchant_images/23/product/main_55_01.jpg', 1, 1, NOW()),
(58, 24, 'main', '/profile/merchant_images/24/product/main_58_01.jpg', 1, 1, NOW()),
(60, 24, 'main', '/profile/merchant_images/24/product/main_60_01.jpg', 1, 1, NOW()),
(62, 25, 'main', '/profile/merchant_images/25/product/main_62_01.jpg', 1, 1, NOW()),
(63, 25, 'main', '/profile/merchant_images/25/product/main_63_01.jpg', 1, 1, NOW()),
(65, 26, 'main', '/profile/merchant_images/26/product/main_65_01.jpg', 1, 1, NOW()),
(68, 27, 'main', '/profile/merchant_images/27/product/main_68_01.jpg', 1, 1, NOW()),
(70, 27, 'main', '/profile/merchant_images/27/product/main_70_01.jpg', 1, 1, NOW());


-- =============================================
-- 第七步：补充团购活动 + 团购商品明细
-- =============================================

INSERT INTO groupon_activity (id, merchant_id, name, cover_image, start_time, end_time, status, source_type, sort, create_time) VALUES
(5, 13, '火锅狂欢季', '/profile/merchant_images/13/groupon/cover_5.jpg', '2026-05-01 00:00:00', '2026-07-31 23:59:59', 1, 'MERCHANT', 1, NOW()),
(6, 14, '东方明珠暑期特惠', '/profile/merchant_images/14/groupon/cover_6.jpg', '2026-06-01 00:00:00', '2026-08-31 23:59:59', 1, 'ADMIN', 1, NOW()),
(7, 16, '大唐宫宴夏日巡演', '/profile/merchant_images/16/groupon/cover_7.jpg', '2026-05-15 00:00:00', '2026-09-15 23:59:59', 1, 'MERCHANT', 1, NOW()),
(8, 17, '欢乐谷暑期亲子月', '/profile/merchant_images/17/groupon/cover_8.jpg', '2026-07-01 00:00:00', '2026-08-31 23:59:59', 0, 'ADMIN', 1, NOW()),
(9, 19, '长隆野生动物世界年中大促', '/profile/merchant_images/19/groupon/cover_9.jpg', '2026-05-20 00:00:00', '2026-06-30 23:59:59', 1, 'MERCHANT', 1, NOW()),
(10, 23, '全聚德烤鸭节', '/profile/merchant_images/23/groupon/cover_10.jpg', '2026-05-01 00:00:00', '2026-06-30 23:59:59', 1, 'MERCHANT', 1, NOW()),
(11, 24, '故宫文化体验月', '/profile/merchant_images/24/groupon/cover_11.jpg', '2026-06-01 00:00:00', '2026-06-30 23:59:59', 0, 'ADMIN', 1, NOW()),
(12, 26, '德云社五月专场', '/profile/merchant_images/26/groupon/cover_12.jpg', '2026-05-01 00:00:00', '2026-05-31 23:59:59', 2, 'MERCHANT', 1, NOW());

-- 团购商品明细
INSERT INTO groupon_activity_item (groupon_id, merchant_id, name, cover_image, original_price, groupon_price, stock, sales, limit_per_user, valid_days, status, sort, create_time) VALUES
-- 蜀香火锅 季
(5, 13, '双人火锅套餐(团购)', '/profile/merchant_images/13/groupon/item_5_1.jpg', 218.00, 158.00, 200, 0, 2, 30, 1, 1, NOW()),
(5, 13, '四人豪华火锅套餐(团购)', '/profile/merchant_images/13/groupon/item_5_2.jpg', 428.00, 298.00, 100, 0, 1, 30, 1, 2, NOW()),
-- 东方明珠 暑期
(6, 14, '二球联票亲子(1大1小)', '/profile/merchant_images/14/groupon/item_6_1.jpg', 240.00, 168.00, 5000, 0, 3, 7, 1, 1, NOW()),
(6, 14, '三球联票+旋转餐厅', '/profile/merchant_images/14/groupon/item_6_2.jpg', 380.00, 268.00, 2000, 0, 2, 7, 1, 2, NOW()),
-- 大唐宫宴
(7, 16, 'VIP演出票+宫宴(单人)', '/profile/merchant_images/16/groupon/item_7_1.jpg', 668.00, 468.00, 200, 0, 3, 30, 1, 1, NOW()),
(7, 16, '双人宫宴套餐(含汉服)', '/profile/merchant_images/16/groupon/item_7_2.jpg', 1280.00, 888.00, 100, 0, 1, 30, 1, 2, NOW()),
-- 欢乐谷 暑期（未开始）
(8, 17, '暑期亲子月卡(1大1小)', '/profile/merchant_images/17/groupon/item_8_1.jpg', 599.00, 399.00, 1000, 0, 1, 60, 1, 1, NOW()),
-- 长隆
(9, 19, '家庭套票(2大1小含餐)', '/profile/merchant_images/19/groupon/item_9_1.jpg', 895.00, 720.00, 3000, 0, 2, 7, 1, 1, NOW()),
(9, 19, '双人票+自驾区升级', '/profile/merchant_images/19/groupon/item_9_2.jpg', 750.00, 580.00, 5000, 0, 3, 7, 1, 2, NOW()),
-- 全聚德
(10, 23, '烤鸭半只+京味四小碟', '/profile/merchant_images/23/groupon/item_10_1.jpg', 286.00, 198.00, 200, 0, 2, 14, 1, 1, NOW()),
(10, 23, '整只烤鸭+6热菜套餐', '/profile/merchant_images/23/groupon/item_10_2.jpg', 888.00, 668.00, 80, 0, 1, 14, 1, 2, NOW()),
-- 故宫（未开始）
(11, 24, '故宫深度讲解+珍宝馆套票', '/profile/merchant_images/24/groupon/item_11_1.jpg', 308.00, 228.00, 1000, 0, 2, 7, 1, 1, NOW()),
-- 德云社（已结束）
(12, 26, '五月晚场相声票+茶座', '/profile/merchant_images/26/groupon/item_12_1.jpg', 178.00, 128.00, 200, 156, 3, 7, 1, 1, NOW());


-- =============================================
-- 第八步：补充商家账号（每个商家 1 管理员 + 1~2 店员）
-- =============================================

-- 密码统一为 BCrypt('123456') = $2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2
INSERT INTO merchant_user (id, merchant_id, username, password, real_name, phone, role, status, create_time) VALUES
-- 蜀香火锅
(6,  13, 'hotpot_admin',   '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '赵店长', '13800001301', 'owner',  1, NOW()),
(7,  13, 'hotpot_staff1',  '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '小刘',   '13800001302', 'member', 1, NOW()),
-- 东方明珠
(8,  14, 'tower_admin',    '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '李店长', '13800001401', 'owner',  1, NOW()),
(9,  14, 'tower_staff1',   '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '小陈',   '13800001402', 'member', 1, NOW()),
-- 外滩民宿
(10, 15, 'bnb_admin',      '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '周店长', '13800001501', 'owner',  1, NOW()),
(11, 15, 'bnb_staff1',     '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '小王',   '13800001502', 'member', 1, NOW()),
-- 大唐宫宴
(12, 16, 'tang_admin',     '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '钱店长', '13800001601', 'owner',  1, NOW()),
(13, 16, 'tang_staff1',    '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '小周',   '13800001602', 'member', 1, NOW()),
(14, 16, 'tang_staff2',    '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '小吴',   '13800001603', 'member', 1, NOW()),
-- 欢乐谷
(15, 17, 'park_admin',     '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '吴店长', '13800001701', 'owner',  1, NOW()),
(16, 17, 'park_staff1',    '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '小郑',   '13800001702', 'member', 1, NOW()),
-- 点都德
(17, 18, 'tea_admin',      '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '陈店长', '13800001801', 'owner',  1, NOW()),
(18, 18, 'tea_staff1',     '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '小黄',   '13800001802', 'member', 1, NOW()),
-- 长隆动物园
(19, 19, 'safari_admin',   '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '林店长', '13800001901', 'owner',  1, NOW()),
-- 企鹅酒店
(20, 20, 'penguin_admin',  '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '黄店长', '13800002001', 'owner',  1, NOW()),
-- 广州大剧院
(21, 21, 'opera_admin',    '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '郑店长', '13800002101', 'owner',  1, NOW()),
-- 密室逃脱
(22, 22, 'room_admin',     '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '冯店长', '13800002201', 'owner',  1, NOW()),
(23, 22, 'room_staff1',    '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '小钱',   '13800002202', 'member', 1, NOW()),
-- 全聚德
(24, 23, 'duck_admin',     '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '孙店长', '13800002301', 'owner',  1, NOW()),
(25, 23, 'duck_staff1',    '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '小马',   '13800002302', 'member', 1, NOW()),
-- 故宫
(26, 24, 'gugong_admin',   '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '马店长', '13800002401', 'owner',  1, NOW()),
-- 胡同民宿
(27, 25, 'hutong_admin',   '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '朱店长', '13800002501', 'owner',  1, NOW()),
-- 德云社
(28, 26, 'deyun_admin',    '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '杨店长', '13800002601', 'owner',  1, NOW()),
-- 万达影城
(29, 27, 'cine_admin',     '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '高店长', '13800002701', 'owner',  1, NOW()),
(30, 27, 'cine_staff1',    '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '小杨',   '13800002702', 'member', 1, NOW());


-- =============================================
-- 第九步：补充 C端测试用户（覆盖不同分销商区域）
-- =============================================

INSERT INTO mall_user (id, nickname, phone, avatar, open_id, status, total_orders, total_amount, create_time) VALUES
(6,  '上海吃货小陈', '15000006666', '', 'oWxShUser006pqr', 1, 0, 0.00, NOW()),
(7,  '广州玩家小林', '15000007777', '', 'oWxGzUser007stu', 1, 0, 0.00, NOW()),
(8,  '北京旅行达人', '15000008888', '', 'oWxBjUser008vwx', 1, 0, 0.00, NOW()),
(9,  '亲子游爱好者', '15000009999', '', 'oWxUser009yzab',  1, 0, 0.00, NOW()),
(10, '文艺青年小李', '15000001010', '', 'oWxUser010cdce',  1, 0, 0.00, NOW());

-- 补 user_info
INSERT INTO user_info (user_id, user_name, user_type, phone, open_id, avatar_url, create_time, update_time) VALUES
('6',  '上海吃货小陈', 'wechat', '15000006666', 'oWxShUser006pqr', '', NOW(), NOW()),
('7',  '广州玩家小林', 'wechat', '15000007777', 'oWxGzUser007stu', '', NOW(), NOW()),
('8',  '北京旅行达人', 'wechat', '15000008888', 'oWxBjUser008vwx', '', NOW(), NOW()),
('9',  '亲子游爱好者', 'wechat', '15000009999', 'oWxUser009yzab',  '', NOW(), NOW()),
('10', '文艺青年小李', 'wechat', '15000001010', 'oWxUser010cdce',  '', NOW(), NOW());


-- =============================================
-- 第十步：补充订单数据（覆盖所有状态 × 多商家 × 多业态）
-- =============================================

INSERT INTO mall_order (order_no, merchant_id, user_id, total_amount, pay_amount, commission, merchant_income, status, pay_time, complete_time, cancel_time, write_off_code, create_time) VALUES
-- 蜀香火锅 (13)
('ORD202605201301', 13, 6, 158.00, 158.00, 15.80, 142.20, 1, '2026-05-20 12:30:00', NULL, NULL, 'WOHP001', '2026-05-20 12:28:00'),
('ORD202605201302', 13, 6, 298.00, 298.00, 29.80, 268.20, 3, '2026-05-18 18:00:00', '2026-05-18 20:30:00', NULL, 'WOHP002', '2026-05-18 17:55:00'),
-- 东方明珠 (14)
('ORD202605211401', 14, 6, 168.00, 168.00, 16.80, 151.20, 2, '2026-05-21 09:00:00', NULL, NULL, 'WOTP001', '2026-05-21 08:55:00'),
('ORD202605151402', 14, 8, 120.00, 120.00, 12.00, 108.00, 3, '2026-05-15 10:00:00', '2026-05-15 15:00:00', NULL, 'WOTP002', '2026-05-15 09:50:00'),
-- 外滩民宿 (15)
('ORD202605221501', 15, 6, 598.00, 598.00, 59.80, 538.20, 1, '2026-05-22 14:00:00', NULL, NULL, 'WOBN001', '2026-05-22 13:50:00'),
-- 大唐宫宴 (16)
('ORD202605191601', 16, 10, 468.00, 468.00, 46.80, 421.20, 3, '2026-05-19 19:00:00', '2026-05-19 21:30:00', NULL, 'WOTG001', '2026-05-19 18:50:00'),
('ORD202605231602', 16, 10, 888.00, 888.00, 88.80, 799.20, 0, NULL, NULL, NULL, NULL, '2026-05-23 20:00:00'),
-- 欢乐谷 (17)
('ORD202605221701', 17, 9, 298.00, 298.00, 29.80, 268.20, 2, '2026-05-22 08:00:00', NULL, NULL, 'WOPK001', '2026-05-22 07:50:00'),
('ORD202605201702', 17, 9, 498.00, 498.00, 49.80, 448.20, 3, '2026-05-20 09:00:00', '2026-05-20 18:00:00', NULL, 'WOPK002', '2026-05-20 08:45:00'),
('ORD202605211703', 17, 9, 599.00, 599.00, 59.90, 539.10, 4, '2026-05-21 10:00:00', NULL, '2026-05-21 10:05:00', NULL, '2026-05-21 09:55:00'),
-- 点都德 (18)
('ORD202605201801', 18, 7, 128.00, 128.00, 12.80, 115.20, 3, '2026-05-20 08:30:00', '2026-05-20 10:00:00', NULL, 'WOTD001', '2026-05-20 08:25:00'),
-- 长隆动物园 (19)
('ORD202605211901', 19, 7, 720.00, 720.00, 72.00, 648.00, 2, '2026-05-21 07:30:00', NULL, NULL, 'WOSF001', '2026-05-21 07:20:00'),
-- 全聚德 (23)
('ORD202605202301', 23, 8, 198.00, 198.00, 19.80, 178.20, 1, '2026-05-20 11:30:00', NULL, NULL, 'WODK001', '2026-05-20 11:25:00'),
('ORD202605182302', 23, 8, 1280.00, 1280.00, 128.00, 1152.00, 3, '2026-05-18 18:00:00', '2026-05-18 20:30:00', NULL, 'WODK002', '2026-05-18 17:50:00'),
-- 故宫 (24)
('ORD202605222401', 24, 8, 258.00, 258.00, 25.80, 232.20, 2, '2026-05-22 08:00:00', NULL, NULL, 'WOGG001', '2026-05-22 07:50:00'),
-- 德云社 (26)
('ORD202605192601', 26, 10, 128.00, 128.00, 12.80, 115.20, 3, '2026-05-19 19:00:00', '2026-05-19 21:30:00', NULL, 'WODY001', '2026-05-19 18:50:00'),
-- 万达影城 (27)
('ORD202605222701', 27, 8, 128.00, 128.00, 12.80, 115.20, 1, '2026-05-22 19:00:00', NULL, NULL, 'WOCI001', '2026-05-22 18:55:00'),
('ORD202605212702', 27, 8, 68.00, 68.00, 6.80, 61.20, 5, NULL, NULL, '2026-05-21 20:00:00', NULL, '2026-05-21 19:50:00');

-- 补充 order_item（关联上面的订单）
INSERT INTO order_item (order_id, merchant_id, product_id, product_name, product_image, price, quantity, subtotal, create_time) VALUES
(11, 13, 21, '双人火锅套餐', '/profile/merchant_images/13/product/cover_21.jpg', 158.00, 1, 158.00, NOW()),
(12, 13, 22, '四人豪华套餐', '/profile/merchant_images/13/product/cover_22.jpg', 298.00, 1, 298.00, NOW()),
(13, 14, 26, '亲子套票(1大1小)', '/profile/merchant_images/14/product/cover_26.jpg', 168.00, 1, 168.00, NOW()),
(14, 14, 23, '二球联票(成人)', '/profile/merchant_images/14/product/cover_23.jpg', 120.00, 1, 120.00, NOW()),
(15, 15, 27, '江景大床房(含早)', '/profile/merchant_images/15/product/cover_27.jpg', 598.00, 1, 598.00, NOW()),
(16, 16, 31, 'VIP席演出票(成人)', '/profile/merchant_images/16/product/cover_31.jpg', 468.00, 1, 468.00, NOW()),
(17, 16, 32, '双人宫宴套餐', '/profile/merchant_images/16/product/cover_32.jpg', 888.00, 1, 888.00, NOW()),
(18, 17, 36, '亲子套票(1大1小)', '/profile/merchant_images/17/product/cover_36.jpg', 298.00, 1, 298.00, NOW()),
(19, 17, 38, '家庭票(2大1小)', '/profile/merchant_images/17/product/cover_38.jpg', 498.00, 1, 498.00, NOW()),
(20, 17, 37, '年卡(成人)', '/profile/merchant_images/17/product/cover_37.jpg', 599.00, 1, 599.00, NOW()),
(21, 18, 39, '经典早茶双人套餐', '/profile/merchant_images/18/product/cover_39.jpg', 128.00, 1, 128.00, NOW()),
(22, 19, 45, '家庭套票(2大1小)', '/profile/merchant_images/19/product/cover_45.jpg', 720.00, 1, 720.00, NOW()),
(23, 23, 54, '招牌烤鸭半只', '/profile/merchant_images/23/product/cover_54.jpg', 198.00, 1, 198.00, NOW()),
(24, 23, 57, '商务宴请套餐(6人)', '/profile/merchant_images/23/product/cover_57.jpg', 1280.00, 1, 1280.00, NOW()),
(25, 24, 58, '故宫门票(旺季成人)', '/profile/merchant_images/24/product/cover_58.jpg', 60.00, 2, 120.00, NOW()),
(25, 24, 59, '珍宝馆门票', '/profile/merchant_images/24/product/cover_59.jpg', 10.00, 2, 20.00, NOW()),
(25, 24, 60, '故宫深度讲解(3小时)', '/profile/merchant_images/24/product/cover_60.jpg', 198.00, 1, 198.00, NOW()),
(26, 26, 67, '茶座套餐(含瓜子花生)', '/profile/merchant_images/26/product/cover_67.jpg', 58.00, 2, 116.00, NOW()),
(27, 27, 70, '双人观影套餐', '/profile/merchant_images/27/product/cover_70.jpg', 128.00, 1, 128.00, NOW()),
(28, 27, 68, 'IMAX厅电影票(成人)', '/profile/merchant_images/27/product/cover_68.jpg', 68.00, 1, 68.00, NOW());

-- 补充 payment_record
INSERT INTO payment_record (order_no, merchant_id, user_id, transaction_id, amount, pay_type, pay_status, pay_time, create_time) VALUES
('ORD202605201301', 13, 6, 'WX20260520130001', 158.00, 'JSAPI', 1, '2026-05-20 12:30:00', NOW()),
('ORD202605201302', 13, 6, 'WX20260518130002', 298.00, 'JSAPI', 1, '2026-05-18 18:00:00', NOW()),
('ORD202605211401', 14, 6, 'WX20260521140001', 168.00, 'JSAPI', 1, '2026-05-21 09:00:00', NOW()),
('ORD202605221501', 15, 6, 'WX20260522150001', 598.00, 'JSAPI', 1, '2026-05-22 14:00:00', NOW()),
('ORD202605191601', 16, 10, 'WX20260519160001', 468.00, 'JSAPI', 1, '2026-05-19 19:00:00', NOW()),
('ORD202605221701', 17, 9, 'WX20260522170001', 298.00, 'JSAPI', 1, '2026-05-22 08:00:00', NOW()),
('ORD202605201801', 18, 7, 'WX20260520180001', 128.00, 'JSAPI', 1, '2026-05-20 08:30:00', NOW()),
('ORD202605211901', 19, 7, 'WX20260521190001', 720.00, 'JSAPI', 1, '2026-05-21 07:30:00', NOW()),
('ORD202605202301', 23, 8, 'WX20260520230001', 198.00, 'JSAPI', 1, '2026-05-20 11:30:00', NOW()),
('ORD202605182302', 23, 8, 'WX20260518230002', 1280.00, 'JSAPI', 1, '2026-05-18 18:00:00', NOW()),
('ORD202605222401', 24, 8, 'WX20260522240001', 258.00, 'JSAPI', 1, '2026-05-22 08:00:00', NOW()),
('ORD202605192601', 26, 10, 'WX20260519260001', 128.00, 'JSAPI', 1, '2026-05-19 19:00:00', NOW()),
('ORD202605222701', 27, 8, 'WX20260522270001', 128.00, 'JSAPI', 1, '2026-05-22 19:00:00', NOW());


-- =============================================
-- 第十一步：更新商家统计数据
-- =============================================

UPDATE merchant SET product_count = (SELECT COUNT(*) FROM product WHERE merchant_id = merchant.id AND del_flag='0') WHERE del_flag='0';
