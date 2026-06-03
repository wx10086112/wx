const util = require('../../utils/util')
const orderApi = require('../../api/order')
const refundApi = require('../../api/refund')
const app = getApp()

Page({
  data: {
    tabs: [
      { label: '全部', value: 'ALL' },
      { label: '待支付', value: 'PENDING_PAY' },
      { label: '待使用', value: 'PAID_UNUSED' },
      { label: '退款/售后', value: 'AFTER_SALE' }
    ],
    currentTab: 'ALL',
    orderList: [],
    orderStats: [],
    loading: true,
    showWriteOffModal: false,
    currentOrder: {}
  },

  onLoad(options) {
    const status = options.status || util.consumePendingOrderFilter()
    if (status) {
      this.setData({ currentTab: status })
    }
    this.loadOrders()
  },

  onShow() {
    this.loadOrders()
  },

  onPullDownRefresh() {
    this.loadOrders().then(() => {
      wx.stopPullDownRefresh()
    })
  },

  normalizeOrder(order) {
    return {
      ...order,
      writeOffDeadlineText: order.writeOffDeadline ? util.formatDate(order.writeOffDeadline, 'YYYY-MM-DD') : ''
    }
  },

  buildOrderStats(orderList = []) {
    const stats = {
      pending: orderList.filter((item) => item.status === 'PENDING_PAY').length,
      unused: orderList.filter((item) => item.status === 'PAID_UNUSED').length,
      afterSale: orderList.filter((item) => ['REFUNDING', 'REFUNDED'].includes(item.status)).length
    }

    return [
      { label: '待支付', value: stats.pending },
      { label: '待使用', value: stats.unused },
      { label: '退款售后', value: stats.afterSale }
    ]
  },

  getFilteredOrders(list) {
    const tab = this.data.currentTab
    if (tab === 'ALL') return list
    if (tab === 'PENDING_PAY') return list.filter((item) => item.status === 'PENDING_PAY')
    if (tab === 'PAID_UNUSED') return list.filter((item) => item.status === 'PAID_UNUSED')
    if (tab === 'AFTER_SALE') {
      return list.filter((item) => ['REFUNDING', 'REFUNDED'].includes(item.status))
    }
    return list
  },

  loadOrders() {
    this.setData({ loading: true })

    const statusParam = this.data.currentTab === 'ALL' ? '' : this.data.currentTab
    return orderApi
      .getOrderList({ status: statusParam })
      .then((res) => {
        const orders = (res.data || res || [])
          .sort((a, b) => (b.createTime || 0) - (a.createTime || 0))
          .map((item) => this.normalizeOrder(item))
        this.setData({
          orderStats: this.buildOrderStats(orders),
          orderList: orders,
          loading: false
        })
      })
      .catch(() => {
        this.setData({
          orderList: [],
          orderStats: this.buildOrderStats([]),
          loading: false
        })
      })
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab
    if (this.data.currentTab === tab) return

    this.setData({ currentTab: tab })
    this.loadOrders()
  },

  onOrderTap(e) {
    const order = e.detail.order
    util.navigateTo(`/pages/order-detail/order-detail?orderNo=${order.orderNo}`)
  },

  onCancelOrder(e) {
    const order = e.detail.order
    util.showModal('取消订单', '确认取消该订单？').then((confirm) => {
      if (!confirm) return
      orderApi.cancelOrder(order.orderNo)
        .then(() => {
          util.showToast('订单已取消', 'success')
          this.loadOrders()
        })
        .catch((err) => {
          util.showToast(err && err.msg ? err.msg : '取消失败')
        })
    })
  },

  onPayOrder(e) {
    const order = e.detail.order
    util.showModal('确认支付', `支付 ¥${((order.payAmount || order.price) / 100).toFixed(2)}`).then((confirm) => {
      if (!confirm) return
      util.showLoading('拉起支付...')
      orderApi.createPayOrder({ orderNo: order.orderNo, openId: app.globalData && app.globalData.userInfo ? app.globalData.userInfo.openId : '' })
        .then((res) => util.requestPayment(res.data || res))
        .then(() => orderApi.queryOrder(order.orderNo))
        .then(() => {
          util.hideLoading()
          util.showToast('支付处理中，请稍后刷新', 'success')
          this.loadOrders()
        })
        .catch((err) => {
          util.hideLoading()
          util.showToast(err && err.message ? err.message : '支付失败')
        })
    })
  },

  onViewCode(e) {
    const order = this.normalizeOrder(e.detail.order)
    this.setData({
      currentOrder: order,
      showWriteOffModal: true
    })
  },

  onRefundOrder(e) {
    const order = e.detail.order
    util.showModal('申请退款', '确认发起退款申请？').then((confirm) => {
      if (!confirm) return
      refundApi.applyRefund({
        orderNo: order.orderNo,
        refundReason: '用户申请退款'
      }).then(() => {
        util.showToast('退款申请已提交', 'success')
        this.setData({ currentTab: 'AFTER_SALE' }, () => this.loadOrders())
      }).catch((err) => {
        util.showToast(err && err.message ? err.message : '申请失败')
      })
    })
  },

  confirmWriteOffResult() {
    this.closeWriteOffModal()
  },

  onRebuyOrder(e) {
    const order = e.detail.order
    util.navigateTo(`/pages/product-detail/product-detail?id=${order.productId}`)
  },

  closeWriteOffModal() {
    this.setData({
      showWriteOffModal: false,
      currentOrder: {}
    })
  },

  preventMove() {},

  goShopping() {
    util.switchTab('/pages/home/home')
  }
})
