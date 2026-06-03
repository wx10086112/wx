const defaultTemplateConfig = {
  templateMeta: {
    code: 'merchant_o2o_v1',
    name: '门店服务',
    version: '1.1.0',
    configOwner: 'merchant-mini-admin',
    description: '门店服务、到店使用和售后服务配置'
  },
  brandInfo: {
    id: 'brand_default',
    name: '商家名称',
    slogan: '',
    notice: '支持微信支付、到店使用、退款售后与消息通知',
    servicePhone: '',
    primaryColor: '#1677ff'
  },
  home: {
    locationLabel: '距离本店',
    noticeTag: '',
    statsCards: [],
    merchantSectionTitle: '本店信息',
    merchantSectionSubtitle: '查看营业时间、门店地址与联系方式',
    productSectionTitle: '本店服务项目',
    productSectionSubtitle: '购买后到店出示使用码即可使用',
    sortOptions: []
  },
  profile: {
    loginTitle: '点击登录',
    loginDesc: '登录后查看订单、到店使用码与售后进度',
    orderSectionTitle: '我的订单',
    orderMoreText: '全部订单 ›',
    orderEntries: [
      { label: '待支付', status: 'PENDING_PAY' },
      { label: '待使用', status: 'UNUSED' },
      { label: '退款/售后', status: 'AFTER_SALE' },
    ],
    assetEntries: [],
    benefitTitle: '订单服务',
    benefitDesc: '订单、核销码和售后进度统一展示，方便到店前快速查看。',
    benefitTips: ['支持到店使用', '支持退款售后'],
    serviceMenus: [
      { label: '联系客服', url: '/pages/contact/contact' }
    ],
    logoutText: '退出登录'
  },
  merchantDetail: {
    hotTag: '热门',
    phoneActionText: '一键拨打',
    mapActionText: '查看地图',
    addressTitle: '门店地址',
    productSectionTitle: '在售项目',
    productSectionSubtitle: '点击查看详情与使用规则',
    albumSectionTitle: '门店相册',
    albumSectionSubtitle: '门头、环境与项目实拍',
    homeNavText: '首页',
    orderNavText: '订单',
    contactButtonText: '联系门店'
  },
  productDetail: {
    decisionSectionTitle: '购买决策信息',
    ruleSectionTitle: '使用规则',
    merchantSectionTitle: '服务门店',
    contentSectionTitle: '项目内容',
    salesLabel: '已售',
    stockLabel: '库存',
    validDaysLabel: '有效期',
    timeRangeRuleText: '使用时间段：以门店营业时间为准',
    bookingYesText: '需要预约',
    bookingNoText: '无需预约',
    shareText: '分享',
    buyButtonText: '立即抢购'
  },
  checkout: {
    productSectionTitle: '确认商品',
    infoSectionTitle: '购买信息',
    priceSectionTitle: '价格明细',
    useRuleSectionTitle: '使用说明',
    quantityLabel: '购买数量',
    phoneLabel: '手机号',
    subtotalLabel: '商品金额',
    totalLabel: '实付总金额',
    paySummaryLabel: '待支付',
    submitButtonText: '提交订单并支付',
    loginHintText: '订单创建后可在订单中心完成支付与到店使用'
  },
  featureToggle: {
    enableCoupon: false,
    enableFavorite: false,
    enableAddress: false,
    enableJoinApply: false,
    enableBookingRule: true,
    enableRefundRule: true,
    enableMerchantAlbum: true
  }
}

module.exports = {
  defaultTemplateConfig
}
