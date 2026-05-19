const util = require('../../utils/util')
const api = require('../../api/index')

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
    if (!app.needLogin()) return
    this.loadData()
  },

  loadData() {
    api
      .getMerchantWorkbenchOverview()
      .then((response) => {
        this.setData({
          staffUser: response.staffUser || app.globalData.staffUser || {},
          storeInfo: response.storeInfo || {},
          statsCardList: [
            { label: '待接单', value: response.stats.pendingAcceptCount || 0, highlight: (response.stats.pendingAcceptCount || 0) > 0 },
            { label: '待核销', value: response.stats.pendingVerifyCount || 0 },
            { label: '已完成', value: response.stats.completedCount || 0 },
            { label: '退款中', value: response.stats.refundingCount || 0, warn: (response.stats.refundingCount || 0) > 0 },
            { label: '在售套餐', value: response.stats.onShelfCount || 0 }
          ],
          quickActionList: this.buildQuickActions(),
          pendingOrderList: (response.pendingOrderList || []).map((item) => ({
            ...item,
            payAmountText: util.formatPrice(item.payAmount),
            payTimeText: util.formatDate(item.payTime)
          }))
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

    // 构建提醒列表
    const alertList = []
    if (stats.pendingAcceptCount > 0) {
      alertList.push({
        type: 'urgent',
        text: `有 ${stats.pendingAcceptCount} 个新订单等待接单，请及时处理！`,
        filter: 'PENDING_ACCEPT'
      })
    }
    if (stats.abnormalCount > 0) {
      alertList.push({
        type: 'warning',
        text: `有 ${stats.abnormalCount} 个异常订单（退款/拒单），请关注处理。`,
        filter: 'REFUNDING'
      })
    }
    const threshold = Number(storeInfo.stockAlertThreshold || 20)
    const lowStockGoods = util.getLowStockGoods(threshold)
    if (lowStockGoods.length > 0) {
      alertList.push({
        type: 'warning',
        text: `${lowStockGoods.length} 个商品库存不足（≤${threshold}），请及时补货。`,
        action: 'goods'
      })
    }

    this.setData({
      staffUser: app.globalData.staffUser || {},
      storeInfo,
      todaySalesText: util.formatPrice(stats.todaySalesAmount),
      alertList,
      statsCardList: [
        { label: '待接单', value: stats.pendingAcceptCount, highlight: stats.pendingAcceptCount > 0 },
        { label: '待核销', value: stats.pendingVerifyCount },
        { label: '已完成', value: stats.completedCount },
        { label: '退款中', value: stats.refundingCount, warn: stats.refundingCount > 0 },
        { label: '在售套餐', value: stats.onShelfCount }
      ],
      quickActionList: this.buildQuickActions(),
      pendingOrderList: orderList
        .filter((item) => ['PENDING_ACCEPT', 'PENDING_VERIFY'].includes(item.status))
        .sort((a, b) => (b.payTime || 0) - (a.payTime || 0))
        .slice(0, 5)
        .map((item) => ({
          ...item,
          payAmountText: util.formatPrice(item.payAmount),
          payTimeText: util.formatDate(item.payTime),
          statusMeta: util.getOrderStatusMeta(item.status)
        }))
    })
  },

  buildQuickActions() {
    return [
      { label: '待接单', icon: '📋', url: '/pages/order/order', permissionCodes: ['order.manage'], isTab: true, filter: 'PENDING_ACCEPT' },
      { label: '扫码核销', icon: '📷', url: '/pages/verify/verify', permissionCodes: ['verify.scan', 'verify.manual'], isTab: true },
      { label: '核销记录', icon: '📝', url: '/pages/verify-records/verify-records', permissionCodes: ['verify.record', 'verify.scan', 'verify.manual'], isTab: false },
      { label: '商品管理', icon: '🏷️', url: '/pages/goods/goods', permissionCodes: ['goods.manage'], isTab: true },
      { label: '财务收益', icon: '💰', url: '/pages/finance/finance', permissionCodes: ['finance.manage'], isTab: false },
      { label: '营销活动', icon: '🎫', url: '/pages/marketing/marketing', permissionCodes: ['goods.manage'], isTab: false },
      { label: '门店设置', icon: '🏪', url: '/pages/store/store', permissionCodes: ['store.manage'], isTab: false },
      { label: '员工权限', icon: '👥', url: '/pages/staff/staff', permissionCodes: ['staff.manage'], isTab: false }
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
    util.navigateTo(`/pages/order-detail/order-detail?orderNo=${e.currentTarget.dataset.orderno}`)
  },

  goAlertOrders(e) {
    const { filter, action } = e.currentTarget.dataset
    if (action === 'goods') {
      util.switchTab('/pages/goods/goods')
      return
    }
    if (filter) {
      util.setPendingOrderFilter(filter)
    }
    util.switchTab('/pages/order/order')
  }
})

