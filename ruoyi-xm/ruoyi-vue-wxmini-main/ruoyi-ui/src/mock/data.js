/**
 * 测试数据集合
 * 所有模块的mock数据集中管理，后期替换为真实API时删除此文件
 */

// ========== 数据中心 ==========
export const dashboardStats = {
  todayAmount: 45920,
  totalFlow: 3680000,
  todayOrders: 328,
  merchantCount: 156,
  userTotal: 12680,
  userTodayNew: 86
}

export const trendData = {
  dates: ['05/03', '05/04', '05/05', '05/06', '05/07', '05/08', '05/09'],
  orderCounts: [280, 310, 295, 340, 328, 365, 328],
  amounts: [38500, 42100, 39800, 47600, 45920, 51300, 45920],
  completedCounts: [220, 260, 240, 290, 275, 310, 285]
}

export const merchantRankList = [
  { id: 1, name: '鲜味火锅城', sales: 52800, orders: 1280, rating: 4.8 },
  { id: 2, name: '日式匠心料理', sales: 41600, orders: 856, rating: 4.7 },
  { id: 3, name: '果鲜优选', sales: 38200, orders: 2100, rating: 4.6 },
  { id: 4, name: '悦健健身', sales: 29800, orders: 645, rating: 4.9 },
  { id: 5, name: '美颜美容馆', sales: 25400, orders: 432, rating: 4.5 },
  { id: 6, name: '川味小馆', sales: 22100, orders: 890, rating: 4.4 },
  { id: 7, name: '甜心烘焙坊', sales: 19800, orders: 560, rating: 4.3 },
  { id: 8, name: '绿叶蔬果店', sales: 17500, orders: 720, rating: 4.2 }
]

// ========== 商家中心 ==========
export const merchantList = [
  { id: 1, name: '鲜味火锅城', contact: '张三', phone: '13800138001', commissionRate: 5, status: 1, createTime: '2026-01-15', products: 28, monthlySales: 52800 },
  { id: 2, name: '日式匠心料理', contact: '李四', phone: '13800138002', commissionRate: 8, status: 1, createTime: '2026-02-03', products: 15, monthlySales: 41600 },
  { id: 3, name: '果鲜优选', contact: '王五', phone: '13800138003', commissionRate: 10, status: 1, createTime: '2026-02-20', products: 42, monthlySales: 38200 },
  { id: 4, name: '悦健健身', contact: '赵六', phone: '13800138004', commissionRate: 10, status: 1, createTime: '2026-03-01', products: 8, monthlySales: 29800 },
  { id: 5, name: '美颜美容馆', contact: '钱七', phone: '13800138005', commissionRate: 15, status: 2, createTime: '2026-03-10', products: 12, monthlySales: 25400 },
  { id: 6, name: '川味小馆', contact: '孙八', phone: '13800138006', commissionRate: 12, status: 1, createTime: '2026-03-18', products: 20, monthlySales: 22100 },
  { id: 7, name: '甜心烘焙坊', contact: '周九', phone: '13800138007', commissionRate: 10, status: 0, createTime: '2026-04-02', products: 35, monthlySales: 19800 },
  { id: 8, name: '绿叶蔬果店', contact: '吴十', phone: '13800138008', commissionRate: 15, status: 1, createTime: '2026-04-15', products: 56, monthlySales: 17500 }
]

export const merchantFlowList = [
  { id: 1, merchantId: 1, merchantName: '鲜味火锅城', type: '订单收入', amount: 2580, time: '2026-05-09 14:30', orderId: 'ORD20260509001' },
  { id: 2, merchantId: 2, merchantName: '日式匠心料理', type: '订单收入', amount: 1680, time: '2026-05-09 13:22', orderId: 'ORD20260509002' },
  { id: 3, merchantId: 3, merchantName: '果鲜优选', type: '提现扣款', amount: -5000, time: '2026-05-09 11:00', orderId: '-' },
  { id: 4, merchantId: 1, merchantName: '鲜味火锅城', type: '退款扣款', amount: -128, time: '2026-05-09 10:15', orderId: 'ORD20260508015' },
  { id: 5, merchantId: 4, merchantName: '悦健健身', type: '订单收入', amount: 399, time: '2026-05-08 16:45', orderId: 'ORD20260508010' },
  { id: 6, merchantId: 6, merchantName: '川味小馆', type: '订单收入', amount: 890, time: '2026-05-08 12:30', orderId: 'ORD20260508008' }
]

export const withdrawList = [
  { id: 1, merchantName: '鲜味火锅城', amount: 10000, status: 0, applyTime: '2026-05-09 09:00', bankName: '工商银行', bankAccount: '****6789' },
  { id: 2, merchantName: '果鲜优选', amount: 5000, status: 1, applyTime: '2026-05-08 14:00', bankName: '建设银行', bankAccount: '****1234', auditTime: '2026-05-08 15:00' },
  { id: 3, merchantName: '美颜美容馆', amount: 3000, status: 2, applyTime: '2026-05-07 10:00', bankName: '农业银行', bankAccount: '****5678', auditTime: '2026-05-07 11:00', completeTime: '2026-05-07 16:00' },
  { id: 4, merchantName: '日式匠心料理', amount: 8000, status: 3, applyTime: '2026-05-06 08:00', bankName: '招商银行', bankAccount: '****9012', auditTime: '2026-05-06 09:00', rejectReason: '账户信息有误' }
]

// ========== 商品数据 ==========
let productIdCounter = 20
export const productList = [
  { id: 1, merchantId: 1, name: '精品双人火锅套餐', category: '美食', originalPrice: 258, price: 198, stock: 100, sales: 1280, status: 1, image: '/product/hotpot.jpg', description: '含锅底+6荤4素+主食', validDays: 30, createTime: '2026-03-01' },
  { id: 2, merchantId: 1, name: '四人豪华海鲜套餐', category: '美食', originalPrice: 498, price: 388, stock: 50, sales: 650, status: 1, image: '/product/seafood.jpg', description: '波龙+大闸蟹+鲍鱼+虾', validDays: 30, createTime: '2026-03-05' },
  { id: 3, merchantId: 1, name: '午市单人工作餐', category: '美食', originalPrice: 45, price: 32, stock: 200, sales: 3200, status: 1, image: '/product/lunch.jpg', description: '一荤两素+米饭+汤', validDays: 7, createTime: '2026-03-10' },
  { id: 4, merchantId: 2, name: '日式料理四人餐', category: '美食', originalPrice: 398, price: 298, stock: 30, sales: 856, status: 1, image: '/product/japanese.jpg', description: '刺身拼盘+寿司+天妇罗', validDays: 15, createTime: '2026-02-15' },
  { id: 5, merchantId: 2, name: '单人刺身定食', category: '美食', originalPrice: 88, price: 68, stock: 80, sales: 1520, status: 1, image: '/product/sashimi.jpg', description: '三文鱼+金枪鱼+甜虾', validDays: 3, createTime: '2026-02-20' },
  { id: 6, merchantId: 3, name: '鲜果拼团10斤装', category: '生鲜', originalPrice: 69, price: 49, stock: 500, sales: 2100, status: 1, image: '/product/fruit_box.jpg', description: '当季时令水果随机搭配', validDays: 7, createTime: '2026-02-20' },
  { id: 7, merchantId: 3, name: '进口车厘子5斤', category: '生鲜', originalPrice: 198, price: 158, stock: 120, sales: 680, status: 1, image: '/product/cherry.jpg', description: '智利进口JJJ级', validDays: 5, createTime: '2026-03-01' },
  { id: 8, merchantId: 3, name: '有机蔬菜周卡', category: '生鲜', originalPrice: 59, price: 39, stock: 300, sales: 720, status: 1, image: '/product/veggie.jpg', description: '每周配送5种有机蔬菜', validDays: 7, createTime: '2026-03-15' },
  { id: 9, merchantId: 3, name: '土鸡蛋30枚装', category: '生鲜', originalPrice: 49, price: 35, stock: 200, sales: 950, status: 1, image: '/product/egg.jpg', description: '散养土鸡蛋，新鲜直发', validDays: 15, createTime: '2026-04-01' },
  { id: 10, merchantId: 4, name: '健身私教体验课', category: '健身', originalPrice: 199, price: 99, stock: 50, sales: 320, status: 1, image: '/product/gym.jpg', description: '1对1私教60分钟', validDays: 30, createTime: '2026-03-01' },
  { id: 11, merchantId: 4, name: '月卡不限次', category: '健身', originalPrice: 399, price: 299, stock: 100, sales: 180, status: 1, image: '/product/gym_monthly.jpg', description: '全时段不限次健身', validDays: 30, createTime: '2026-03-10' },
  { id: 12, merchantId: 5, name: '面部深层清洁套餐', category: '美容', originalPrice: 298, price: 198, stock: 80, sales: 210, status: 1, image: '/product/skincare.jpg', description: '深层清洁+补水面膜', validDays: 30, createTime: '2026-03-15' },
  { id: 13, merchantId: 5, name: '美甲套餐', category: '美容', originalPrice: 168, price: 128, stock: 60, sales: 350, status: 1, image: '/product/nail.jpg', description: '纯色+款式任选', validDays: 30, createTime: '2026-03-20' },
  { id: 14, merchantId: 6, name: '川菜四人套餐', category: '美食', originalPrice: 188, price: 138, stock: 100, sales: 890, status: 1, image: '/product/sichuan.jpg', description: '水煮鱼+麻婆豆腐+回锅肉', validDays: 15, createTime: '2026-03-18' },
  { id: 15, merchantId: 7, name: '生日蛋糕定制', category: '烘焙', originalPrice: 168, price: 128, stock: 30, sales: 420, status: 1, image: '/product/cake.jpg', description: '8寸动物奶油蛋糕', validDays: 1, createTime: '2026-04-02' },
  { id: 16, merchantId: 7, name: '法式可颂6只装', category: '烘焙', originalPrice: 48, price: 35, stock: 150, sales: 980, status: 0, image: '/product/croissant.jpg', description: '原味+巧克力+杏仁', validDays: 5, createTime: '2026-04-05' },
  { id: 17, merchantId: 8, name: '有机蔬菜月卡', category: '生鲜', originalPrice: 199, price: 159, stock: 200, sales: 380, status: 1, image: '/product/veggie_monthly.jpg', description: '每周配送，共4次', validDays: 30, createTime: '2026-04-15' },
  { id: 18, merchantId: 8, name: '水果礼盒装', category: '生鲜', originalPrice: 128, price: 98, stock: 80, sales: 260, status: 1, image: '/product/fruit_gift.jpg', description: '精选6种进口水果', validDays: 7, createTime: '2026-04-20' }
]

export function getNextProductId() {
  return ++productIdCounter
}

// ========== 入驻审核数据 ==========
export const auditList = [
  { id: 1, merchantId: 5, name: '美颜美容馆', contact: '钱七', phone: '13800138005', applyTime: '2026-03-10', status: 0, reason: '' },
  { id: 2, merchantId: 9, name: '潮汕牛肉火锅', contact: '林老板', phone: '13800138009', applyTime: '2026-05-08', status: 0, reason: '' },
  { id: 3, merchantId: 10, name: '星空咖啡馆', contact: '陈店长', phone: '13800138010', applyTime: '2026-05-09', status: 0, reason: '' },
  { id: 4, merchantId: 6, name: '川味小馆', contact: '孙八', phone: '13800138006', applyTime: '2026-03-18', status: 1, reason: '' },
  { id: 5, merchantId: 3, name: '果鲜优选', contact: '王五', phone: '13800138003', applyTime: '2026-02-20', status: 1, reason: '' },
  { id: 6, merchantId: 11, name: '花间集花艺', contact: '花花', phone: '13800138011', applyTime: '2026-05-01', status: 2, reason: '资质不全，请补充营业执照' }
]

// ========== 订单中心 ==========
export const orderList = [
  { id: 1, orderNo: 'ORD20260509001', merchantId: 1, merchantName: '鲜味火锅城', userName: '用户A', goodsName: '精品双人火锅套餐', amount: 258, payAmount: 198, status: 1, createTime: '2026-05-09 14:30', payTime: '2026-05-09 14:32' },
  { id: 2, orderNo: 'ORD20260509002', merchantId: 2, merchantName: '日式匠心料理', userName: '用户B', goodsName: '日式料理四人餐', amount: 398, payAmount: 298, status: 0, createTime: '2026-05-09 13:22', payTime: null },
  { id: 3, orderNo: 'ORD20260509003', merchantId: 3, merchantName: '果鲜优选', userName: '用户C', goodsName: '鲜果拼团10斤装', amount: 69, payAmount: 49, status: 2, createTime: '2026-05-09 11:00', payTime: '2026-05-09 11:05' },
  { id: 4, orderNo: 'ORD20260508015', merchantId: 1, merchantName: '鲜味火锅城', userName: '用户D', goodsName: '精品双人火锅套餐', amount: 258, payAmount: 198, status: 3, createTime: '2026-05-08 10:15', payTime: '2026-05-08 10:18' },
  { id: 5, orderNo: 'ORD20260508010', merchantId: 4, merchantName: '悦健健身', userName: '用户E', goodsName: '健身私教体验课', amount: 99, payAmount: 79, status: 2, createTime: '2026-05-08 16:45', payTime: '2026-05-08 16:48' },
  { id: 6, orderNo: 'ORD20260508008', merchantId: 6, merchantName: '川味小馆', userName: '用户F', goodsName: '川菜四人套餐', amount: 188, payAmount: 138, status: 1, createTime: '2026-05-08 12:30', payTime: '2026-05-08 12:33' },
  { id: 7, orderNo: 'ORD20260507005', merchantId: 7, merchantName: '甜心烘焙坊', userName: '用户G', goodsName: '生日蛋糕定制', amount: 168, payAmount: 128, status: 4, createTime: '2026-05-07 09:00', payTime: '2026-05-07 09:05' },
  { id: 8, orderNo: 'ORD20260507003', merchantId: 8, merchantName: '绿叶蔬果店', userName: '用户H', goodsName: '有机蔬菜周卡', amount: 59, payAmount: 39, status: 5, createTime: '2026-05-07 08:00', payTime: '2026-05-07 08:02' }
]

// 订单状态: 0待付款 1已付款 2已完成 3已退款 4售后中 5异常
export const orderStatusMap = {
  0: { text: '待付款', type: 'warning' },
  1: { text: '已付款', type: 'primary' },
  2: { text: '已完成', type: 'success' },
  3: { text: '已退款', type: 'info' },
  4: { text: '售后中', type: 'danger' },
  5: { text: '异常', type: 'danger' }
}

export const afterSaleList = [
  { id: 1, orderNo: 'ORD20260508015', merchantName: '鲜味火锅城', userName: '用户D', reason: '食材不新鲜', amount: 198, status: 0, applyTime: '2026-05-08 18:00' },
  { id: 2, orderNo: 'ORD20260507005', merchantName: '甜心烘焙坊', userName: '用户G', reason: '蛋糕损坏', amount: 128, status: 1, applyTime: '2026-05-07 15:00' },
  { id: 3, orderNo: 'ORD20260506012', merchantName: '果鲜优选', userName: '用户I', reason: '水果腐烂', amount: 49, status: 2, applyTime: '2026-05-06 10:00' }
]

export const abnormalOrderList = [
  { id: 1, orderNo: 'ORD20260507003', merchantName: '绿叶蔬果店', userName: '用户H', issue: '支付超时未回调', amount: 39, createTime: '2026-05-07 08:00', status: 0 },
  { id: 2, orderNo: 'ORD20260505020', merchantName: '川味小馆', userName: '用户J', issue: '库存不足但仍下单成功', amount: 138, createTime: '2026-05-05 19:00', status: 1 }
]

// ========== 财务中心 ==========
export const platformFlowList = [
  { id: 1, type: '订单收入', amount: 258, merchantName: '鲜味火锅城', orderNo: 'ORD20260509001', time: '2026-05-09 14:32', commission: 20.64 },
  { id: 2, type: '订单收入', amount: 398, merchantName: '日式匠心料理', orderNo: 'ORD20260509002', time: '2026-05-09 13:25', commission: 31.84 },
  { id: 3, type: '退款支出', amount: -198, merchantName: '鲜味火锅城', orderNo: 'ORD20260508015', time: '2026-05-08 18:05', commission: -15.84 },
  { id: 4, type: '订单收入', amount: 69, merchantName: '果鲜优选', orderNo: 'ORD20260509003', time: '2026-05-09 11:05', commission: 6.90 },
  { id: 5, type: '订单收入', amount: 99, merchantName: '悦健健身', orderNo: 'ORD20260508010', time: '2026-05-08 16:48', commission: 7.92 }
]

export const profitShareList = [
  { id: 1, merchantName: '鲜味火锅城', orderNo: 'ORD20260509001', orderAmount: 258, commissionRate: 8, commission: 20.64, merchantIncome: 237.36, status: 1, time: '2026-05-09 14:32' },
  { id: 2, merchantName: '日式匠心料理', orderNo: 'ORD20260509002', orderAmount: 398, commissionRate: 8, commission: 31.84, merchantIncome: 366.16, status: 1, time: '2026-05-09 13:25' },
  { id: 3, merchantName: '果鲜优选', orderNo: 'ORD20260509003', orderAmount: 69, commissionRate: 10, commission: 6.90, merchantIncome: 62.10, status: 0, time: '2026-05-09 11:05' }
]

export const financeReport = {
  totalRevenue: 3680000,
  totalCommission: 328000,
  totalWithdraw: 2850000,
  totalRefund: 186000,
  netProfit: 962000,
  monthlyData: [
    { month: '2026-01', revenue: 520000, commission: 46800, orders: 3200 },
    { month: '2026-02', revenue: 480000, commission: 43200, orders: 2900 },
    { month: '2026-03', revenue: 610000, commission: 54900, orders: 3800 },
    { month: '2026-04', revenue: 580000, commission: 52200, orders: 3500 },
    { month: '2026-05', revenue: 45920, commission: 4132, orders: 328 }
  ]
}

export const incomeStats = {
  todayIncome: 45920,
  monthIncome: 580000,
  totalIncome: 3680000,
  todayCommission: 4132,
  monthCommission: 52200,
  totalCommission: 328000
}

// ========== 数据分析 ==========
export const salesStats = {
  totalSales: 3680000,
  totalOrders: 28500,
  avgOrderAmount: 129,
  conversionRate: 3.8,
  categoryData: [
    { name: '美食', sales: 1520000, percent: 41.3 },
    { name: '生鲜', sales: 860000, percent: 23.4 },
    { name: '美容', sales: 520000, percent: 14.1 },
    { name: '健身', sales: 430000, percent: 11.7 },
    { name: '其他', sales: 350000, percent: 9.5 }
  ]
}

export const orderStats = {
  totalOrders: 28500,
  completedOrders: 24200,
  refundOrders: 890,
  abnormalOrders: 120,
  dailyData: [
    { date: '05/03', newOrders: 280, completed: 220, refund: 12 },
    { date: '05/04', newOrders: 310, completed: 260, refund: 8 },
    { date: '05/05', newOrders: 295, completed: 240, refund: 15 },
    { date: '05/06', newOrders: 340, completed: 290, refund: 10 },
    { date: '05/07', newOrders: 328, completed: 275, refund: 6 },
    { date: '05/08', newOrders: 365, completed: 310, refund: 9 },
    { date: '05/09', newOrders: 328, completed: 285, refund: 11 }
  ]
}

// ========== 系统管理 ==========
export const loginLogList = [
  { id: 1, userName: 'admin', ip: '192.168.1.100', location: '局域网', browser: 'Chrome 120', os: 'Windows 11', status: 0, loginTime: '2026-05-09 14:30:00' },
  { id: 2, userName: 'admin', ip: '192.168.1.100', location: '局域网', browser: 'Chrome 120', os: 'Windows 11', status: 0, loginTime: '2026-05-09 09:00:00' },
  { id: 3, userName: 'ry', ip: '192.168.1.105', location: '局域网', browser: 'Edge 120', os: 'Windows 11', status: 1, loginTime: '2026-05-08 16:00:00' },
  { id: 4, userName: 'admin', ip: '10.0.0.55', location: '内网', browser: 'Firefox 115', os: 'Windows 10', status: 0, loginTime: '2026-05-08 08:30:00' },
  { id: 5, userName: 'test', ip: '192.168.1.200', location: '局域网', browser: 'Chrome 119', os: 'macOS', status: 1, loginTime: '2026-05-07 14:00:00' }
]

export const operationLogList = [
  { id: 1, operator: 'admin', module: '商家管理', operation: '审核商家', method: 'MerchantController.audit', ip: '192.168.1.100', time: '2026-05-09 14:25:00', status: 0, costTime: 58 },
  { id: 2, operator: 'admin', module: '订单管理', operation: '查询订单列表', method: 'OrderController.list', ip: '192.168.1.100', time: '2026-05-09 14:20:00', status: 0, costTime: 120 },
  { id: 3, operator: 'admin', module: '商家管理', operation: '修改商家抽成比例', method: 'MerchantController.updateCommission', ip: '192.168.1.100', time: '2026-05-09 11:30:00', status: 0, costTime: 45 },
  { id: 4, operator: 'ry', module: '订单管理', operation: '导出订单', method: 'OrderController.export', ip: '192.168.1.105', time: '2026-05-08 16:10:00', status: 0, costTime: 2300 },
  { id: 5, operator: 'admin', module: '系统管理', operation: '修改菜单', method: 'MenuController.edit', ip: '192.168.1.100', time: '2026-05-08 10:00:00', status: 1, costTime: 30 }
]
