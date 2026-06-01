const util = require('../../../utils/merchant-util')
const api = require('../../../api/merchant-mini/index')

const app = getApp()

Page({
  data: {
    staffUser: {},
    storeInfo: {},
    statsCardList: [],
    quickActionList: [],
    pendingOrderList: [],
    alertList: [],
    todaySalesText: '0.00'
  },

  onShow() {
    if (!app.needMerchantLogin()) return
    this.loadData()
  },

  loadData() {
    api
      .getMerchantWorkbenchOverview()
      .then((response = {}) => {
        const stats = response.stats || {}
        this.setData({
          staffUser: response.staffUser || app.globalData.staffUser || {},
          storeInfo: response.storeInfo || util.getStoreInfo(),
          todaySalesText: util.formatPrice(stats.todaySalesAmount),
          statsCardList: this.buildStatsCardList(stats),
          alertList: this.buildAlertList(stats),
          quickActionList: this.buildQuickActions(),
          pendingOrderList: this.buildPendingOrderList(response.pendingOrderList || [])
        })
      })
      .catch(() => {
        this.loadLocalData()
      })
  },

  loadLocalData() {
    const storeInfo = util.getStoreInfo()
    const goodsList = util.getGoodsList()
    const orderList = util.getOrderList()
    const stats = util.buildWorkbenchStats(orderList, goodsList)
    const threshold = Number(storeInfo.stockAlertThreshold || 20)
    const lowStockGoods = util.getLowStockGoods(threshold)
    const alertList = this.buildAlertList(stats, lowStockGoods.length, threshold)

    this.setData({
      staffUser: app.globalData.staffUser || {},
      storeInfo,
      todaySalesText: util.formatPrice(stats.todaySalesAmount),
      alertList,
      statsCardList: this.buildStatsCardList(stats),
      quickActionList: this.buildQuickActions(),
      pendingOrderList: this.buildPendingOrderList(orderList)
    })
  },

  buildStatsCardList(stats = {}) {
    return [
      { label: '待核销', value: stats.pendingVerifyCount || 0, highlight: (stats.pendingVerifyCount || 0) > 0 },
      { label: '已完成', value: stats.completedCount || 0 },
      { label: '退款中', value: stats.refundingCount || 0, warn: (stats.refundingCount || 0) > 0 },
      { label: '在售套餐', value: stats.onShelfCount || 0 }
    ]
  },

  buildAlertList(stats = {}, lowStockCount = 0, threshold = 20) {
    const alertList = []
    if ((stats.refundingCount || 0) > 0) {
      alertList.push({
        type: 'warning',
        text: `有 ${stats.refundingCount} 个退款订单等待处理。`,
        filter: 'REFUNDING'
      })
    }
    if (lowStockCount > 0) {
      alertList.push({
        type: 'warning',
        text: `${lowStockCount} 个商品库存不足（≤${threshold}），请及时补货。`,
        action: 'goods'
      })
    }
    return alertList
  },

  buildPendingOrderList(orderList = []) {
    return util
      .normalizeGrouponOrders(orderList)
      .filter((item) => ['PENDING_VERIFY', 'REFUNDING'].includes(item.status))
      .sort((a, b) => (b.payTime || 0) - (a.payTime || 0))
      .slice(0, 5)
      .map((item) => ({
        ...item,
        payAmountText: util.formatPrice(item.payAmount),
        payTimeText: util.formatDate(item.payTime),
        statusMeta: util.getOrderStatusMeta(item.status)
      }))
  },

  buildQuickActions() {
    return [
      { label: '待核销', icon: '📋', url: '/pages/merchant/order/order', permissionCodes: ['order.manage'], isTab: true, filter: 'PENDING_VERIFY' },
      { label: '扫码核销', icon: '📷', url: '/pages/merchant/verify/verify', permissionCodes: ['verify.scan', 'verify.manual'], isTab: true },
      { label: '核销记录', icon: '📝', url: '/pages/merchant/verify-records/verify-records', permissionCodes: ['verify.record', 'verify.scan', 'verify.manual'], isTab: false },
      { label: '商品管理', icon: '🏷️', url: '/pages/merchant/goods/goods', permissionCodes: ['goods.manage'], isTab: true },
      { label: '结算中心', icon: '💰', url: '/pages/merchant/finance/finance', permissionCodes: ['finance.manage'], isTab: false },
      { label: '营销活动', icon: '🎫', url: '/pages/merchant/marketing/marketing', permissionCodes: ['goods.manage'], isTab: false },
      { label: '门店设置', icon: '🏪', url: '/pages/merchant/store/store', permissionCodes: ['store.manage'], isTab: false },
      { label: '员工权限', icon: '👥', url: '/pages/merchant/staff/staff', permissionCodes: ['staff.manage'], isTab: false }
    ].filter((item) => app.hasAnyPermission(item.permissionCodes))
  },

  toggleBusinessStatus() {
    if (!app.needPermission(['store.manage'])) return
    const storeInfo = {
      ...this.data.storeInfo,
      businessStatus: !this.data.storeInfo.businessStatus
    }
    util.setStoreInfo(storeInfo)
    this.setData({ storeInfo })
    util.showToast(storeInfo.businessStatus ? '已切换为营业中' : '已切换为休息中', 'success')
  },

  goQuickAction(e) {
    const { url, istab, filter } = e.currentTarget.dataset
    if (filter) {
      util.setPendingOrderFilter(filter)
    }
    if (istab) {
      util.switchTab(url)
      return
    }
    util.navigateTo(url)
  },

  goOrderDetail(e) {
    util.navigateTo(`/pages/merchant/order-detail/order-detail?orderNo=${e.currentTarget.dataset.orderno}`)
  },

  goAlertOrders(e) {
    const { filter, action } = e.currentTarget.dataset
    if (action === 'goods') {
      util.switchTab('/pages/merchant/goods/goods')
      return
    }
    if (filter) {
      util.setPendingOrderFilter(filter)
    }
    util.switchTab('/pages/merchant/order/order')
  }
})

