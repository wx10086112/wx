const util = require('../../../utils/merchant-util')
const api = require('../../../api/merchant-mini/index')

const app = getApp()

const tabs = [
  { label: '全部', value: 'ALL' },
  { label: '待核销', value: 'PENDING_VERIFY' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '退款中', value: 'REFUNDING' },
  { label: '已退款', value: 'REFUNDED' },
  { label: '已取消', value: 'CANCELLED' }
]

const normalizeTab = (tab) => (tabs.some((item) => item.value === tab) ? tab : 'ALL')

Page({
  data: {
    tabs,
    currentTab: 'ALL',
    orderList: [],
    cancelModalVisible: false,
    cancelReason: '',
    cancelOrderNo: '',
    merchantNavList: util.getMerchantNavList('order')
  },

  onLoad(options) {
    const currentTab = normalizeTab(options.tab || util.consumePendingOrderFilter())
    this.setData({ currentTab })
  },

  onShow() {
    if (!app.needMerchantLogin()) return
    const currentTab = util.consumePendingOrderFilter()
    if (currentTab) {
      this.setData({ currentTab: normalizeTab(currentTab) })
    }
    this.loadData()
  },

  loadData() {
    const requestStatus = this.data.currentTab === 'ALL' ? '' : this.data.currentTab
    api
      .getMerchantOrderList({ status: requestStatus })
      .then((response) => {
        this.renderOrderList(response || [])
      })
      .catch(() => {
        this.renderOrderList(util.getOrderList())
      })
  },

  renderOrderList(orderList = []) {
    const displayList = util
      .normalizeGrouponOrders(orderList)
      .sort((a, b) => (b.payTime || 0) - (a.payTime || 0))
      .map((item) => ({
        ...item,
        statusMeta: util.getOrderStatusMeta(item.status),
        payAmountText: util.formatPrice(item.payAmount),
        payTimeText: util.formatDate(item.payTime || item.createTime)
      }))

    this.setData({
      orderList: this.filterOrders(displayList, this.data.currentTab)
    })
  },

  filterOrders(orderList = [], tab = 'ALL') {
    if (tab === 'ALL') return orderList
    return orderList.filter((item) => item.status === tab)
  },

  switchTab(e) {
    const tab = normalizeTab(e.currentTarget.dataset.tab)
    this.setData({ currentTab: tab }, () => {
      this.loadData()
    })
  },

  goOrderDetail(e) {
    util.navigateTo(`/pages/merchant/order-detail/order-detail?orderNo=${e.currentTarget.dataset.orderno}`)
  },

  goVerify(e) {
    if (!app.needPermission(['verify.scan', 'verify.manual'])) return
    util.navigateTo(`/pages/merchant/verify/verify?orderNo=${e.currentTarget.dataset.orderno}`)
  },

  goRefundReview(e) {
    util.navigateTo(`/pages/merchant/order-detail/order-detail?orderNo=${e.currentTarget.dataset.orderno}`)
  },

  handleCancelOrder(e) {
    if (!app.needPermission(['order.manage'])) return
    const orderNo = e.currentTarget.dataset.orderno
    this.setData({
      cancelModalVisible: true,
      cancelReason: '',
      cancelOrderNo: orderNo
    })
  },

  handleCancelReasonInput(e) {
    this.setData({
      cancelReason: e.detail.value
    })
  },

  closeCancelModal() {
    this.setData({
      cancelModalVisible: false,
      cancelReason: '',
      cancelOrderNo: ''
    })
  },

  confirmCancelOrder() {
    const orderNo = this.data.cancelOrderNo
    const reason = this.data.cancelReason.trim()
    if (!orderNo) return
    api.cancelMerchantOrder(orderNo, { reason })
      .then(() => {
        util.showToast('订单已取消', 'success')
        this.closeCancelModal()
        this.loadData()
      })
      .catch(() => {
        const result = util.cancelOrder(orderNo, reason)
        util.showToast(result.message, result.success ? 'success' : 'none')
        if (result.success) {
          this.closeCancelModal()
          this.loadData()
        }
      })
  },

  goMerchantTab(e) {
    const { url } = e.currentTarget.dataset
    if (url) {
      util.openMerchantMainPage(url)
    }
  }
})
