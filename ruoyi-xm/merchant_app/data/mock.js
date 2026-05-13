const permissionOptions = [
  { label: '查看经营数据', value: 'stats.view' },
  { label: '订单处理', value: 'order.manage' },
  { label: '扫码核销', value: 'verify.scan' },
  { label: '手动核销', value: 'verify.manual' },
  { label: '核销记录', value: 'verify.record' },
  { label: '商品管理', value: 'goods.manage' },
  { label: '门店设置', value: 'store.manage' },
  { label: '员工权限', value: 'staff.manage' },
  { label: '财务收益', value: 'finance.manage' }
]

const roleTemplates = {
  manager: {
    roleKey: 'manager',
    roleName: '店长',
    permissions: permissionOptions.map((item) => item.value)
  },
  clerk: {
    roleKey: 'clerk',
    roleName: '店员',
    permissions: ['stats.view', 'order.manage', 'verify.scan', 'verify.manual', 'verify.record']
  }
}

const staffList = [
  {
    staffId: 1,
    name: '林店长',
    phone: '13800001111',
    roleKey: 'manager',
    roleName: '店长',
    status: 'ACTIVE',
    permissions: roleTemplates.manager.permissions
  },
  {
    staffId: 2,
    name: '周店员',
    phone: '13800002222',
    roleKey: 'clerk',
    roleName: '店员',
    status: 'ACTIVE',
    permissions: roleTemplates.clerk.permissions
  }
]

const merchantInfo = {
  merchantId: 1,
  storeId: 1,
  brandName: '蓝屿轻养生活馆',
  storeName: '蓝屿轻养·国贸旗舰店',
  shortName: '国贸店',
  brandSlogan: '单店团购到店核销运营端',
  notice: '支持扫码核销、手动核销、商品上下架与单店员工权限管理',
  businessHours: '10:00-22:00',
  phone: '010-88886601',
  address: '北京市朝阳区建国路88号嘉里中心B1',
  latitude: 39.9087,
  longitude: 116.4591,
  avatar: '/assets/images/merchant-spa.png',
  coverImage: '/assets/images/merchant-spa.png',
  serviceTags: ['到店核销', '团购套餐', '营业中'],
  bannerTitles: ['午市轻养专场', '肩颈护理次卡', '晚间舒压热卖'],
  businessStatus: true,
  supportRefund: true,
  supportBooking: true
}

const goodsList = [
  {
    goodsId: 101,
    title: '芳香舒压 SPA 90 分钟',
    subtitle: '精油舒缓 + 热敷放松 + 独立房间',
    imageUrl: '',
    price: 19800,
    originalPrice: 39800,
    stock: 88,
    sales: 2651,
    validPeriod: '2026-05-01 至 2026-06-30',
    verifyNotice: '到店出示核销码即可使用',
    status: 'ON_SHELF',
    categoryName: 'SPA轻养',
    sort: 1
  },
  {
    goodsId: 102,
    title: '肩颈理疗放松套餐 60 分钟',
    subtitle: '久坐人群推荐，到店即用',
    imageUrl: '',
    price: 13800,
    originalPrice: 26800,
    stock: 126,
    sales: 1942,
    validPeriod: '2026-05-01 至 2026-07-15',
    verifyNotice: '高峰期建议提前电话确认',
    status: 'ON_SHELF',
    categoryName: '肩颈理疗',
    sort: 2
  },
  {
    goodsId: 103,
    title: '都市焕肤护理 75 分钟',
    subtitle: '清洁补水 + 舒缓修护',
    imageUrl: '',
    price: 16800,
    originalPrice: 32800,
    stock: 72,
    sales: 1129,
    validPeriod: '2026-05-01 至 2026-06-20',
    verifyNotice: '建议提前 1 天预约',
    status: 'ON_SHELF',
    categoryName: '面部护理',
    sort: 3
  },
  {
    goodsId: 104,
    title: '过期测试团购券',
    subtitle: '用于验证过期核销拦截',
    imageUrl: '',
    price: 9900,
    originalPrice: 19800,
    stock: 10,
    sales: 12,
    validPeriod: '2026-04-01 至 2026-04-30',
    verifyNotice: '过期后不可核销',
    status: 'OFF_SHELF',
    categoryName: '系统测试',
    sort: 4
  }
]

const orderList = [
  {
    orderId: 1,
    orderNo: 'M202605090001',
    goodsId: 101,
    title: '芳香舒压 SPA 90 分钟',
    customerName: '王女士',
    customerPhone: '138****2201',
    quantity: 1,
    payAmount: 18800,
    status: 'PENDING_VERIFY',
    createTime: 1778269200000,
    payTime: 1778269800000,
    writeOffCode: 'LY8012'
  },
  {
    orderId: 2,
    orderNo: 'M202605090002',
    goodsId: 102,
    title: '肩颈理疗放松套餐 60 分钟',
    customerName: '赵先生',
    customerPhone: '139****3202',
    quantity: 1,
    payAmount: 13800,
    status: 'PENDING_VERIFY',
    createTime: 1778272800000,
    payTime: 1778273100000,
    writeOffCode: 'LY9321'
  },
  {
    orderId: 3,
    orderNo: 'M202605080003',
    goodsId: 101,
    title: '芳香舒压 SPA 90 分钟',
    customerName: '孙女士',
    customerPhone: '136****1103',
    quantity: 1,
    payAmount: 18800,
    status: 'COMPLETED',
    createTime: 1778186400000,
    payTime: 1778187000000,
    writeOffCode: 'LY7710',
    verifyTime: 1778190000000,
    verifyStaffName: '周店员'
  },
  {
    orderId: 4,
    orderNo: 'M202605070004',
    goodsId: 103,
    title: '都市焕肤护理 75 分钟',
    customerName: '何女士',
    customerPhone: '137****5004',
    quantity: 1,
    payAmount: 16800,
    status: 'REFUNDING',
    createTime: 1778100000000,
    payTime: 1778100600000,
    writeOffCode: 'LY5508',
    refundReason: '临时无法到店'
  },
  {
    orderId: 5,
    orderNo: 'M202604300005',
    goodsId: 104,
    title: '过期测试团购券',
    customerName: '陈先生',
    customerPhone: '135****9005',
    quantity: 1,
    payAmount: 9900,
    status: 'PENDING_VERIFY',
    createTime: 1777564800000,
    payTime: 1777565400000,
    writeOffCode: 'LY0005'
  }
]

const buildStaffUser = (roleKey = 'manager') => {
  const role = roleTemplates[roleKey] || roleTemplates.manager
  const seedUser = staffList.find((item) => item.roleKey === roleKey) || staffList[0]
  return {
    staffId: seedUser.staffId,
    name: seedUser.name,
    phone: seedUser.phone,
    roleKey: role.roleKey,
    roleName: role.roleName,
    permissions: role.permissions
  }
}

module.exports = {
  permissionOptions,
  roleTemplates,
  staffList,
  merchantInfo,
  goodsList,
  orderList,
  buildStaffUser
}
