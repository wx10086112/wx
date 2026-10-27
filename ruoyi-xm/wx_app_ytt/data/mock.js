const merchantInfo = {
  merchantId: 1,
  storeId: 1,
  storeName: '蓝屿轻养·国贸旗舰店',
  shortName: '国贸店',
  brandName: '蓝屿轻养生活馆',
  brandSlogan: '',
  notice: '支持扫码核销、手动核销、商品上下架与单店员工权限管理',
  businessHours: '10:00-22:00',
  businessHoursText: '周一至周日 10:00-22:00',
  phone: '010-88886601',
  address: '北京市朝阳区建国路88号嘉里中心B1',
  latitude: 39.9087,
  longitude: 116.4591,
  avatar: '/assets/images/merchant-spa.png',
  coverImage: '/assets/images/merchant-spa.png',
  sales: 3260,
  serviceTags: ['到店核销', '服务项目', '营业中'],
  serviceAbilityTags: ['到店核销', '可预约', '支持退款', '过期自动退'],
  facilityTags: ['免费停车', '独立房间', '安静环境', '专业护理'],
  albumList: ['/assets/images/merchant-spa.png', '/assets/images/merchant-neck.png', '/assets/images/merchant-spa.png'],
  bannerTitles: ['午市轻养专场', '肩颈护理次卡', '晚间舒压热卖'],
  businessStatus: true,
  supportRefund: true,
  supportBooking: true
}

const merchantList = [
  {
    id: merchantInfo.storeId,
    merchantId: merchantInfo.merchantId,
    name: merchantInfo.storeName,
    shortName: merchantInfo.shortName,
    avatar: merchantInfo.avatar,
    coverImage: merchantInfo.coverImage,
    sales: merchantInfo.sales,
    address: merchantInfo.address,
    distance: '650m',
    distanceValue: 650,
    categoryId: 0,
    categoryName: '本店全部服务',
    businessHours: merchantInfo.businessHours,
    businessHoursText: merchantInfo.businessHoursText,
    phone: merchantInfo.phone,
    latitude: merchantInfo.latitude,
    longitude: merchantInfo.longitude,
    tags: merchantInfo.serviceTags,
    serviceAbilityTags: merchantInfo.serviceAbilityTags,
    facilityTags: merchantInfo.facilityTags,
    albumList: merchantInfo.albumList,
    notice: merchantInfo.notice,
    isHot: true,
    businessStatus: merchantInfo.businessStatus
  }
]

const grouponList = [
  {
    id: 101,
    goodsId: 101,
    title: '芳香舒压 SPA 90 分钟',
    subtitle: '精油舒缓 + 热敷放松 + 独立房间',
    merchantId: merchantInfo.storeId,
    merchantName: merchantInfo.storeName,
    image: '/assets/images/merchant-spa.png',
    originalPrice: 39800,
    price: 19800,
    sales: 2651,
    stock: 88,
    validDays: 30,
    validPeriod: '2026-05-01 至 2026-06-30',
    categoryId: 2,
    categoryName: 'SPA轻养',
    tags: ['热销', '到店使用'],
    description: '适合上班族下班放松，支持晚间到店。',
    contentDetail: ['90 分钟芳香舒压护理', '肩颈热敷 1 次', '草本茶饮 1 份'],
    bookingRequired: false,
    bookingRule: '高峰时段建议提前 2 小时电话确认。',
    refundRule: '未使用支持随时退，过期自动退。',
    limitRule: '每个账号限购 3 份。',
    status: 'ON_SHELF',
    sort: 1
  },
  {
    id: 102,
    goodsId: 102,
    title: '肩颈理疗放松套餐 60 分钟',
    subtitle: '久坐人群推荐，到店即用',
    merchantId: merchantInfo.storeId,
    merchantName: merchantInfo.storeName,
    image: '/assets/images/merchant-neck.png',
    originalPrice: 26800,
    price: 13800,
    sales: 1942,
    stock: 126,
    validDays: 45,
    validPeriod: '2026-05-01 至 2026-07-15',
    categoryId: 3,
    categoryName: '肩颈理疗',
    tags: ['免预约', '人气'],
    description: '针对肩颈僵硬、腰背疲劳提供快速放松护理。',
    contentDetail: ['60 分钟肩颈理疗', '姿态评估 1 次', '放松拉伸指导 1 次'],
    bookingRequired: false,
    bookingRule: '日常时段无需预约，节假日建议提前咨询。',
    refundRule: '未使用支持随时退。',
    limitRule: '单次最多购买 5 份，可转赠。',
    status: 'ON_SHELF',
    sort: 2
  },
  {
    id: 103,
    goodsId: 103,
    title: '都市焕肤护理 75 分钟',
    subtitle: '清洁补水 + 舒缓修护',
    merchantId: merchantInfo.storeId,
    merchantName: merchantInfo.storeName,
    image: '/assets/images/merchant-spa.png',
    originalPrice: 32800,
    price: 16800,
    sales: 1129,
    stock: 72,
    validDays: 30,
    validPeriod: '2026-05-01 至 2026-06-20',
    categoryId: 4,
    categoryName: '面部护理',
    tags: ['补水焕肤', '限时'],
    description: '适合熬夜党，护理后可直接带妆离店。',
    contentDetail: ['深层清洁 1 次', '补水修护 1 次', '舒缓面膜 1 次'],
    bookingRequired: true,
    bookingRule: '建议提前 1 天预约。',
    refundRule: '预约前可退，过期自动退。',
    limitRule: '不限购。',
    status: 'ON_SHELF',
    sort: 3
  }
]

const bannerList = [
  {
    id: 1,
    image: '/assets/images/merchant-spa.png',
    title: '新人专享 到店立减 20 元',
    linkType: 'product',
    linkId: 101
  },
  {
    id: 2,
    image: '/assets/images/merchant-neck.png',
    title: '肩颈理疗工作日随到随用',
    linkType: 'product',
    linkId: 102
  },
  {
    id: 3,
    image: '/assets/images/merchant-spa.png',
    title: '都市焕肤护理限时热卖',
    linkType: 'product',
    linkId: 103
  }
]

const categoryList = [
  { id: 0, name: '全部服务' },
  { id: 2, name: 'SPA轻养' },
  { id: 3, name: '肩颈理疗' },
  { id: 4, name: '面部护理' }
]

const couponList = [
  {
    couponId: 1,
    couponName: '到店立减 10 元',
    amount: 1000,
    thresholdAmount: 9900,
    status: 'AVAILABLE',
    validEndTime: '2026-05-31'
  },
  {
    couponId: 2,
    couponName: '满 199 减 30',
    amount: 3000,
    thresholdAmount: 19900,
    status: 'AVAILABLE',
    validEndTime: '2026-06-15'
  },
  {
    couponId: 3,
    couponName: '护理体验券',
    amount: 800,
    thresholdAmount: 6800,
    status: 'USED',
    validEndTime: '2026-05-01'
  }
]

const addressList = [
  {
    addressId: 1,
    consignee: '张三',
    phone: '13800001111',
    province: '北京市',
    city: '北京市',
    district: '朝阳区',
    detailAddress: '建国路 88 号 2 单元 1203',
    isDefault: true
  }
]

const favoriteList = [
  {
    id: 1,
    type: 'product',
    title: '芳香舒压 SPA 90 分钟',
    subtitle: '精油舒缓 + 热敷放松 + 独立房间',
    image: '/assets/images/merchant-spa.png',
    price: 19800
  },
  {
    id: 2,
    type: 'merchant',
    title: merchantInfo.storeName,
    subtitle: '服务项目 / 到店使用 / 单店服务',
    image: merchantInfo.coverImage,
    price: 0
  }
]

const userInfo = {
  userId: 'user_001',
  openId: 'oK1qf5XXXXXXXXXXXXXXXX',
  nickName: '微信用户',
  avatarUrl: '/assets/images/avatar.svg',
  phone: '138****8888',
  gender: 0,
  createTime: 1777348800000
}

const orderList = [
  {
    id: 1,
    orderNo: 'ORD202605080001',
    productId: 101,
    merchantId: merchantInfo.storeId,
    title: '芳香舒压 SPA 90 分钟',
    merchantName: merchantInfo.storeName,
    image: '/assets/images/merchant-spa.png',
    quantity: 1,
    orderAmount: 19800,
    couponAmount: 1000,
    payAmount: 18800,
    price: 18800,
    phone: '138****8888',
    status: 'PAID_UNUSED',
    createTime: 1778256000000,
    payTime: 1778256300000,
    writeOffCode: 'LY20260605A7K9M2QX',
    writeOffDeadline: 1780848000000
  },
  {
    id: 2,
    orderNo: 'ORD202605070002',
    productId: 103,
    merchantId: merchantInfo.storeId,
    title: '都市焕肤护理 75 分钟',
    merchantName: merchantInfo.storeName,
    image: '/assets/images/merchant-spa.png',
    quantity: 1,
    orderAmount: 16800,
    couponAmount: 0,
    payAmount: 16800,
    price: 16800,
    phone: '138****8888',
    status: 'USED_COMPLETED',
    createTime: 1778169600000,
    payTime: 1778169720000,
    writeOffCode: 'LY20260604B8N4T6RP',
    writeOffTime: 1778252400000
  },
  {
    id: 3,
    orderNo: 'ORD202605060003',
    productId: 102,
    merchantId: merchantInfo.storeId,
    title: '肩颈理疗放松套餐 60 分钟',
    merchantName: merchantInfo.storeName,
    image: '/assets/images/merchant-neck.png',
    quantity: 1,
    orderAmount: 13800,
    couponAmount: 0,
    payAmount: 13800,
    price: 13800,
    phone: '138****8888',
    status: 'PENDING_PAY',
    createTime: 1778083200000,
    expireTime: 1778085000000
  },
  {
    id: 4,
    orderNo: 'ORD202605050004',
    productId: 101,
    merchantId: merchantInfo.storeId,
    title: '芳香舒压 SPA 90 分钟',
    merchantName: merchantInfo.storeName,
    image: '/assets/images/merchant-spa.png',
    quantity: 1,
    orderAmount: 19800,
    couponAmount: 1000,
    payAmount: 18800,
    price: 18800,
    phone: '138****8888',
    status: 'REFUNDING',
    createTime: 1777996800000,
    payTime: 1777997100000,
    refundReason: '临时行程变更',
    refundTime: 1778086800000
  }
]

module.exports = {
  merchantInfo,
  merchantList,
  grouponList,
  orderList,
  userInfo,
  bannerList,
  categoryList,
  couponList,
  addressList,
  favoriteList
}
