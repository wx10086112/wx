const util = require('../../utils/util')
const api = require('../../api/index')

const app = getApp()

Page({
  data: {
    tabs: [
      { label: '全部', value: 'ALL' },
      { label: '待接单', value: 'PENDING_ACCEPT' },
      { label: '待核销', value: 'PENDING_VERIFY' },
      { label: '已完成', value: 'COMPLETED' },
      { label: '退款中', value: 'REFUNDING' }
    ],
    currentTab: 'ALL',
    orderList: []
  },

  onLoad(options) {
    const currentTab = options.tab || util.consumePendingOrderFilter()
    if (currentTab) {
      this.setData({ currentTab })
    }
  },

  onShow() {
    if (!app.needLogin()) return
    const currentTab = util.consumePendingOrderFilter()
    if (currentTab) {
      this.setData({ currentTab })
    }
    this.loadData()
  },

  loadData() {
    api
      .getMerchantOrderList({
        status: this.data.currentTab === 'ALL' ? '' : this.data.currentTab
      })
      .then((response) => {
        this.setData({
          orderList: (response || []).map((item) => ({
            ...item,
            statusMeta: util.getOrderStatusMeta(item.status),
            payAmountText: util.formatPrice(item.payAmount),
            payTimeText: util.formatDate(item.payTime || item.createTime)
          }))
        })
      })
      .catch(() => {
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
      })
  },

  filterOrders(orderList = [], tab = 'ALL') {
    if (tab === 'ALL') return orderList
    return orderList.filter((item) => item.status === tab)
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab
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

  handleAcceptOrder(e) {
    if (!app.needPermission(['order.manage'])) return
    const orderNo = e.currentTarget.dataset.orderno
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

  handleCompleteOrder(e) {
    if (!app.needPermission(['order.manage'])) return
    const orderNo = e.currentTarget.dataset.orderno
    util.showModal('完成确认', '确定该订单已完成吗？').then((confirm) => {
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
    })
  }
})
