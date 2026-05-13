const util = require('../../utils/util')
const api = require('../../api/index')

const app = getApp()

Page({
  data: {
    staffUser: {},
    storeInfo: {},
    statsCardList: [],
    quickActionList: [],
    pendingOrderList: []
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
            { label: '待核销', value: response.stats.pendingVerifyCount || 0 },
            { label: '已完成', value: response.stats.completedCount || 0 },
            { label: '退款中', value: response.stats.refundingCount || 0 },
            { label: '在售套餐', value: response.stats.onShelfCount || 0 }
          ],
          quickActionList: [
            { label: '待核销订单', url: '/pages/order/order', permissionCodes: ['order.manage'], isTab: true, filter: 'PENDING_VERIFY' },
            { label: '扫码核销', url: '/pages/verify/verify', permissionCodes: ['verify.scan', 'verify.manual'], isTab: true },
            { label: '核销记录', url: '/pages/verify-records/verify-records', permissionCodes: ['verify.record', 'verify.scan', 'verify.manual'], isTab: false },
            { label: '商品管理', url: '/pages/goods/goods', permissionCodes: ['goods.manage'], isTab: true },
            { label: '财务收益', url: '/pages/finance/finance', permissionCodes: ['finance.manage'], isTab: false },
            { label: '门店设置', url: '/pages/store/store', permissionCodes: ['store.manage'], isTab: false },
            { label: '员工权限', url: '/pages/staff/staff', permissionCodes: ['staff.manage'], isTab: false }
          ].filter((item) => app.hasAnyPermission(item.permissionCodes)),
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
    const quickActionList = [
      { label: '待核销订单', url: '/pages/order/order', permissionCodes: ['order.manage'], isTab: true, filter: 'PENDING_VERIFY' },
      { label: '扫码核销', url: '/pages/verify/verify', permissionCodes: ['verify.scan', 'verify.manual'], isTab: true },
      { label: '核销记录', url: '/pages/verify-records/verify-records', permissionCodes: ['verify.record', 'verify.scan', 'verify.manual'], isTab: false },
      { label: '商品管理', url: '/pages/goods/goods', permissionCodes: ['goods.manage'], isTab: true },
      { label: '财务收益', url: '/pages/finance/finance', permissionCodes: ['finance.manage'], isTab: false },
      { label: '门店设置', url: '/pages/store/store', permissionCodes: ['store.manage'], isTab: false },
      { label: '员工权限', url: '/pages/staff/staff', permissionCodes: ['staff.manage'], isTab: false }
    ].filter((item) => app.hasAnyPermission(item.permissionCodes))

    this.setData({
      staffUser: app.globalData.staffUser || {},
      storeInfo,
      statsCardList: [
        { label: '待核销', value: stats.pendingVerifyCount },
        { label: '已完成', value: stats.completedCount },
        { label: '退款中', value: stats.refundingCount },
        { label: '在售套餐', value: stats.onShelfCount }
      ],
      quickActionList,
      pendingOrderList: orderList
        .filter((item) => item.status === 'PENDING_VERIFY')
        .sort((a, b) => (b.payTime || 0) - (a.payTime || 0))
        .slice(0, 3)
        .map((item) => ({
          ...item,
          payAmountText: util.formatPrice(item.payAmount),
          payTimeText: util.formatDate(item.payTime)
        }))
    })
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
  }
})
