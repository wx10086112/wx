const util = require('../../../utils/merchant-util')
const api = require('../../../api/merchant-mini/index')
const merchantMock = require('../../../data/merchant-mock')

const app = getApp()

const navList = [
  { key: 'workbench', label: '工作台', icon: 'workbench' },
  { key: 'order', label: '订单', icon: 'order' },
  { key: 'verify', label: '核销', icon: 'verify' },
  { key: 'goods', label: '商品', icon: 'goods' },
  { key: 'mine', label: '我的', icon: 'mine' }
]

const orderTabs = [
  { label: '全部', value: 'ALL' },
  { label: '待核销', value: 'PENDING_VERIFY' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '退款中', value: 'REFUNDING' }
]

const goodsTabs = [
  { label: '全部', value: 'ALL' },
  { label: '上架中', value: 'ON_SHELF' },
  { label: '已下架', value: 'OFF_SHELF' }
]

const buildImageCropStyle = (crop = {}) => {
  const scale = Math.min(Math.max(Number(crop.scale || 1), 1), 2.2)
  const renderedPercent = 130 * scale
  const limit = ((renderedPercent - 100) / (renderedPercent * 2)) * 100
  const x = Math.min(Math.max(Number(crop.x || 0), -limit), limit)
  const y = Math.min(Math.max(Number(crop.y || 0), -limit), limit)
  return `transform: translate(${x}%, ${y}%) scale(${scale});`
}

Page({
  data: {
    currentTab: 'workbench',
    navList,
    staffUser: {},
    storeInfo: {},
    todaySalesText: '0.00',
    statsCardList: [],
    quickActionList: [],
    alertList: [],
    pendingOrderList: [],
    orderTabs,
    orderCurrentTab: 'ALL',
    orderList: [],
    goodsTabs,
    goodsCurrentTab: 'ALL',
    goodsList: [],
    manualCode: '',
    verifyResult: null,
    recentRecordList: [],
    operationList: [],
    managementList: []
  },

  onLoad(options = {}) {
    this.loadedTabs = {}
    this.setData({ currentTab: this.normalizeMainTab(options.tab) })
  },

  onShow() {
    if (!app.needMerchantLogin()) return
    util.initMerchantMockStorage(merchantMock)
    this.loadCurrentTab()
  },

  normalizeMainTab(tab = '') {
    return navList.some((item) => item.key === tab) ? tab : 'workbench'
  },

  switchMainTab(e) {
    const currentTab = this.normalizeMainTab(e.currentTarget.dataset.tab)
    if (currentTab === this.data.currentTab) return
    this.setData({ currentTab }, () => this.loadCurrentTab())
  },

  loadCurrentTab() {
    if (this.loadedTabs && this.loadedTabs[this.data.currentTab]) return
    const loaders = {
      workbench: this.loadWorkbench,
      order: this.loadOrders,
      verify: this.loadVerifyRecords,
      goods: this.loadGoods,
      mine: this.loadMine
    }
    const loader = loaders[this.data.currentTab]
    if (loader) {
      this.loadedTabs[this.data.currentTab] = true
      loader.call(this)
    }
  },

  loadWorkbench() {
    api.getMerchantWorkbenchOverview()
      .then((response = {}) => this.renderWorkbench(response))
      .catch(() => {
        const orderList = util.getOrderList()
        const goodsList = util.getGoodsList()
        this.renderWorkbench({
          staffUser: app.globalData.staffUser || merchantMock.buildStaffUser('owner'),
          storeInfo: util.getStoreInfo(),
          stats: util.buildWorkbenchStats(orderList, goodsList),
          pendingOrderList: orderList
        })
      })
  },

  renderWorkbench(response = {}) {
    const stats = response.stats || {}
    const storeInfo = response.storeInfo || util.getStoreInfo()
    const threshold = Number(storeInfo.stockAlertThreshold || 20)
    this.setData({
      staffUser: response.staffUser || app.globalData.staffUser || {},
      storeInfo,
      todaySalesText: util.formatPrice(stats.todaySalesAmount),
      statsCardList: this.buildStatsCardList(stats),
      alertList: this.buildAlertList(stats, util.getLowStockGoods(threshold).length, threshold),
      quickActionList: this.buildQuickActions(),
      pendingOrderList: this.buildPendingOrderList(response.pendingOrderList || [])
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
      alertList.push({ text: `有 ${stats.refundingCount} 个退款订单等待处理。`, tab: 'order', filter: 'REFUNDING' })
    }
    if (lowStockCount > 0) {
      alertList.push({ text: `${lowStockCount} 个商品库存不足（≤${threshold}），请及时补货。`, tab: 'goods' })
    }
    return alertList
  },

  buildPendingOrderList(orderList = []) {
    return util.normalizeGrouponOrders(orderList)
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
      { label: '待核销', tab: 'order', filter: 'PENDING_VERIFY', permissionCodes: ['order.manage'] },
      { label: '扫码核销', tab: 'verify', permissionCodes: ['verify.scan', 'verify.manual'] },
      { label: '商品管理', tab: 'goods', permissionCodes: ['goods.manage'] },
      { label: '结算中心', url: '/pages/merchant/finance/finance', permissionCodes: ['finance.manage'] },
      { label: '门店设置', url: '/pages/merchant/store/store', permissionCodes: ['store.manage'] },
      { label: '员工权限', url: '/pages/merchant/staff/staff', permissionCodes: ['staff.manage'] }
    ].filter((item) => app.hasAnyPermission(item.permissionCodes))
  },

  goAction(e) {
    const { tab, url, filter } = e.currentTarget.dataset
    if (filter) {
      this.loadedTabs.order = false
      this.setData({ orderCurrentTab: filter })
    }
    if (tab) {
      this.setData({ currentTab: tab }, () => this.loadCurrentTab())
      return
    }
    if (url) util.navigateTo(url)
  },

  toggleBusinessStatus() {
    if (!app.needPermission(['store.manage'])) return
    const storeInfo = { ...this.data.storeInfo, businessStatus: !this.data.storeInfo.businessStatus }
    util.setStoreInfo(storeInfo)
    this.setData({ storeInfo })
    util.showToast(storeInfo.businessStatus ? '已切换为营业中' : '已切换为休息中', 'success')
  },

  loadOrders() {
    const requestStatus = this.data.orderCurrentTab === 'ALL' ? '' : this.data.orderCurrentTab
    api.getMerchantOrderList({ status: requestStatus })
      .then((response) => this.renderOrders(response || []))
      .catch(() => this.renderOrders(util.getOrderList()))
  },

  renderOrders(orderList = []) {
    const displayList = util.normalizeGrouponOrders(orderList)
      .sort((a, b) => (b.payTime || 0) - (a.payTime || 0))
      .filter((item) => this.data.orderCurrentTab === 'ALL' || item.status === this.data.orderCurrentTab)
      .map((item) => ({
        ...item,
        statusMeta: util.getOrderStatusMeta(item.status),
        payAmountText: util.formatPrice(item.payAmount),
        payTimeText: util.formatDate(item.payTime || item.createTime)
      }))
    this.setData({ orderList: displayList })
  },

  switchOrderTab(e) {
    this.loadedTabs.order = false
    this.setData({ orderCurrentTab: e.currentTarget.dataset.tab || 'ALL' }, () => this.loadOrders())
  },

  goOrderDetail(e) {
    util.navigateTo(`/pages/merchant/order-detail/order-detail?orderNo=${e.currentTarget.dataset.orderno}`)
  },

  goVerifyOrder(e) {
    const orderNo = e.currentTarget.dataset.orderno
    const target = util.getOrderList().find((item) => item.orderNo === orderNo) || {}
    this.setData({ currentTab: 'verify', manualCode: target.writeOffCode || orderNo }, () => this.loadVerifyRecords())
  },

  loadGoods() {
    api.getMerchantGoodsList()
      .then((goodsList = []) => {
        util.setGoodsList(goodsList)
        this.renderGoods(goodsList)
      })
      .catch(() => this.renderGoods(util.getGoodsList()))
  },

  renderGoods(sourceList = []) {
    const goodsList = sourceList
      .filter((item) => this.data.goodsCurrentTab === 'ALL' || item.status === this.data.goodsCurrentTab)
      .sort((a, b) => (a.sort || 0) - (b.sort || 0))
      .map((item) => ({
        ...item,
        priceText: util.formatPrice(item.price),
        originalPriceText: util.formatPrice(item.originalPrice),
        imageCropStyle: buildImageCropStyle(item.imageCrop),
        lowStock: item.status === 'ON_SHELF' && Number(item.stock || 0) <= 20
      }))
    this.setData({ goodsList })
  },

  switchGoodsTab(e) {
    this.loadedTabs.goods = false
    this.setData({ goodsCurrentTab: e.currentTarget.dataset.tab || 'ALL' }, () => this.loadGoods())
  },

  goEditGoods(e) {
    const goodsId = e.currentTarget.dataset.id
    util.navigateTo(goodsId ? `/pages/merchant/goods-edit/goods-edit?goodsId=${goodsId}` : '/pages/merchant/goods-edit/goods-edit')
  },

  loadVerifyRecords() {
    api.getVerifyRecordList()
      .then((response) => this.renderVerifyRecords(response || []))
      .catch(() => this.renderVerifyRecords(util.getVerifyRecordList()))
  },

  renderVerifyRecords(recordList = []) {
    this.setData({
      recentRecordList: recordList.slice(0, 5).map((item) => ({
        ...item,
        verifyTimeText: util.formatDate(item.verifyTime),
        amountLabel: item.status === 'FAILED' ? '失败' : `¥${util.formatPrice(item.payAmount)}`
      }))
    })
  },

  handleCodeInput(e) {
    this.setData({ manualCode: e.detail.value.trim() })
  },

  scanCode() {
    if (!app.needPermission(['verify.scan'])) return
    wx.scanCode({
      success: (res) => this.processVerifyCode((res.result || '').trim()),
      fail: () => util.showToast('未完成扫码，请改用手动输入')
    })
  },

  submitManualVerify() {
    if (!app.needPermission(['verify.manual'])) return
    if (!this.data.manualCode) {
      util.showToast('请输入核销码')
      return
    }
    this.processVerifyCode(this.data.manualCode)
  },

  processVerifyCode(code) {
    api.writeOffByCode(code)
      .then((response) => {
        this.setData({
          verifyResult: {
            ...response,
            payAmountText: util.formatPrice(response.payAmount),
            verifyTimeText: util.formatDate(response.verifyTime)
          },
          manualCode: ''
        })
        util.showToast('核销成功', 'success')
        this.loadVerifyRecords()
      })
      .catch(() => {
        const result = util.verifyOrderByCode(code, app.globalData.staffUser || {})
        this.setData({
          verifyResult: result.order ? {
            ...result.order,
            payAmountText: util.formatPrice(result.order.payAmount),
            verifyTimeText: util.formatDate(result.order.verifyTime)
          } : null,
          manualCode: result.success ? '' : this.data.manualCode
        })
        util.showToast(result.message, result.success ? 'success' : 'none')
        this.loadVerifyRecords()
      })
  },

  loadMine() {
    const permissionCodes = app.globalData.permissionCodes || []
    this.setData({
      staffUser: app.globalData.staffUser || {},
      storeInfo: util.getStoreInfo(),
      operationList: [
        { title: '核销记录', url: '/pages/merchant/verify-records/verify-records', permissionCodes: ['verify.record', 'verify.scan', 'verify.manual'] },
        { title: '订单管理', tab: 'order', permissionCodes: ['order.manage'] },
        { title: '商品套餐', tab: 'goods', permissionCodes: ['goods.manage'] }
      ].filter((item) => app.hasAnyPermission(item.permissionCodes)),
      managementList: [
        { title: '门店资料', url: '/pages/merchant/store/store', permissionCodes: ['store.manage'] },
        { title: '员工账号', url: '/pages/merchant/staff/staff', permissionCodes: ['staff.manage'] },
        { title: '结算中心', url: '/pages/merchant/finance/finance', permissionCodes: ['finance.manage'] }
      ].filter((item) => app.hasAnyPermission(item.permissionCodes || permissionCodes))
    })
  },

  logout() {
    app.clearMerchantLoginInfo()
    wx.redirectTo({ url: '/pages/merchant/login/login' })
  }
})
