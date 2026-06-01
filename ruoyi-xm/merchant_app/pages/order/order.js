const util = require('../../utils/util')
const api = require('../../api/index')

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
<<<<<<< HEAD
    tabs: [
      { label: '全部', value: 'ALL' },
      { label: '待接单', value: 'PENDING_ACCEPT' },
      { label: '待核销', value: 'PENDING_VERIFY' },
      { label: '配送中', value: 'SHIPPING' },
      { label: '已完成', value: 'COMPLETED' },
      { label: '退款中', value: 'REFUNDING' }
    ],
=======
    tabs,
>>>>>>> 苏
    currentTab: 'ALL',
    orderList: [],
    cancelModalVisible: false,
    cancelReason: '',
    cancelOrderNo: ''
  },

  onLoad(options) {
    const currentTab = normalizeTab(options.tab || util.consumePendingOrderFilter())
    this.setData({ currentTab })
  },

  onShow() {
    if (!app.needLogin()) return
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
<<<<<<< HEAD
        const orderList = util
          .getOrderList()
          .sort((a, b) => (b.payTime || 0) - (a.payTime || 0))
          .map((item) => ({
            ...item,
            statusMeta: util.getOrderStatusMeta(item.status),
            payAmountText: util.formatPrice(item.payAmount),
            payTimeText: util.formatDate(item.payTime || item.createTime)
          }))

        this.setData({
          orderList: this.filterOrders(orderList, this.data.currentTab)
        })
=======
        this.renderOrderList(util.getOrderList())
>>>>>>> 苏
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
    util.navigateTo(`/pages/order-detail/order-detail?orderNo=${e.currentTarget.dataset.orderno}`)
  },

  goVerify(e) {
    if (!app.needPermission(['verify.scan', 'verify.manual'])) return
    util.navigateTo(`/pages/verify/verify?orderNo=${e.currentTarget.dataset.orderno}`)
  },

  goRefundReview(e) {
    util.navigateTo(`/pages/order-detail/order-detail?orderNo=${e.currentTarget.dataset.orderno}`)
  },

  handleCancelOrder(e) {
    if (!app.needPermission(['order.manage'])) return
    const orderNo = e.currentTarget.dataset.orderno
<<<<<<< HEAD
    util.showModal('接单确认', '确定要接受该订单吗？').then((confirm) => {
      if (!confirm) return
      api.acceptMerchantOrder(orderNo)
        .then(() => {
          util.showToast('接单成功', 'success')
          this.loadData()
        })
        .catch(() => {
          const result = util.acceptOrder(orderNo)
          util.showToast(result.message, result.success ? 'success' : 'none')
          this.loadData()
        })
    })
  },

  handleRejectOrder(e) {
    if (!app.needPermission(['order.manage'])) return
    const orderNo = e.currentTarget.dataset.orderno
    util.showModalWithInput('拒单原因', '请输入拒绝接单的原因').then((reason) => {
      if (reason === null) return
      api.rejectMerchantOrder(orderNo, { reason })
        .then(() => {
          util.showToast('已拒单', 'success')
          this.loadData()
        })
        .catch(() => {
          const result = util.rejectOrder(orderNo, reason)
          util.showToast(result.message, result.success ? 'success' : 'none')
          this.loadData()
        })
    })
  },

  handleShipOrder(e) {
    if (!app.needPermission(['order.manage'])) return
    const orderNo = e.currentTarget.dataset.orderno
    util.showModal('发货确认', '确定要标记该订单为配送中吗？').then((confirm) => {
      if (!confirm) return
      api.shipMerchantOrder(orderNo)
        .then(() => {
          util.showToast('已发货', 'success')
          this.loadData()
        })
        .catch(() => {
          const result = util.shipOrder(orderNo)
          util.showToast(result.message, result.success ? 'success' : 'none')
          this.loadData()
        })
    })
  },

  handleCompleteOrder(e) {
    if (!app.needPermission(['order.manage'])) return
    const orderNo = e.currentTarget.dataset.orderno
    util.showModal('完成确认', '确定该订单已送达完成吗？').then((confirm) => {
      if (!confirm) return
      api.completeMerchantOrder(orderNo)
        .then(() => {
          util.showToast('订单已完成', 'success')
          this.loadData()
        })
        .catch(() => {
          const result = util.completeOrder(orderNo)
          util.showToast(result.message, result.success ? 'success' : 'none')
          this.loadData()
        })
=======
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
>>>>>>> 苏
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
  }
})
