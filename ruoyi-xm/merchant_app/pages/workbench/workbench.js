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
        util.showToast('加载失败，请重试')
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
    util.showToast('请在门店设置中修改营业状态')
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
