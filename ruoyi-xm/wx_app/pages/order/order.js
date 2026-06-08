const util = require('../../utils/util')
const agreement = require('../../utils/agreement')
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
    allOrderList: [],
    orderList: [],
    orderStats: [],
    loading: true
  },

  onLoad(options) {
    const status = options.status || util.consumePendingOrderFilter()
    if (status) {
      this.setData({ currentTab: status })
    }

    if (app.globalData.isLoggedIn) {
      this.loadOrders()
    } else {
      this.setData({
        allOrderList: [],
        orderList: [],
        orderStats: this.buildOrderStats([]),
        loading: false
      })
    }
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 1 })
    }
    if (app.globalData.isLoggedIn) {
      this.loadOrders()
    }
  },

  onPullDownRefresh() {
    this.loadOrders().then(() => {
      wx.stopPullDownRefresh()
    })
  },

  normalizeOrder(order = {}) {
    return {
      ...order,
      writeOffDeadlineText: order.writeOffDeadline ? util.formatDate(order.writeOffDeadline, 'YYYY-MM-DD') : ''
    }
  },

  buildOrderStats(orderList = []) {
    const pendingCount = orderList.filter((item) => item.status === 'PENDING_PAY').length
    const unusedCount = orderList.filter((item) => item.status === 'PAID_UNUSED').length
    const afterSaleCount = orderList.filter((item) => ['REFUNDING', 'REFUNDED'].includes(item.status)).length

    return [
      { label: '待支付', value: pendingCount, tab: 'PENDING_PAY' },
      { label: '待使用', value: unusedCount, tab: 'PAID_UNUSED' },
      { label: '退款/售后', value: afterSaleCount, tab: 'AFTER_SALE' }
    ]
  },

  getFilteredOrders(orderList = [], tab = this.data.currentTab) {
    if (tab === 'ALL') {
      return orderList
    }
    if (tab === 'PENDING_PAY') {
      return orderList.filter((item) => item.status === 'PENDING_PAY')
    }
    if (tab === 'PAID_UNUSED') {
      return orderList.filter((item) => item.status === 'PAID_UNUSED')
    }
    if (tab === 'AFTER_SALE') {
      return orderList.filter((item) => ['REFUNDING', 'REFUNDED'].includes(item.status))
    }
    return orderList
  },

  applyCurrentFilter() {
    const filteredOrders = this.getFilteredOrders(this.data.allOrderList, this.data.currentTab)
    this.setData({
      orderList: filteredOrders
    })
  },

  loadOrders() {
    this.setData({ loading: true })

    return orderApi
      .getOrderList()
      .then((res) => {
        const allOrderList = (res.data || res || [])
          .sort((a, b) => (b.createTime || 0) - (a.createTime || 0))
          .map((item) => this.normalizeOrder(item))

        this.setData(
          {
            allOrderList,
            orderStats: this.buildOrderStats(allOrderList),
            loading: false
          },
          () => this.applyCurrentFilter()
        )
      })
      .catch(() => {
        this.setData({
          allOrderList: [],
          orderList: [],
          orderStats: this.buildOrderStats([]),
          loading: false
        })
      })
  },

  setCurrentTab(tab) {
    if (!tab || this.data.currentTab === tab) {
      return
    }

    this.setData(
      {
        currentTab: tab
      },
      () => this.applyCurrentFilter()
    )
  },

  switchTab(e) {
    this.setCurrentTab(e.currentTarget.dataset.tab)
  },

  onQuickFilterTap(e) {
    this.setCurrentTab(e.currentTarget.dataset.tab)
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
    if (!agreement.assertAgreementAccepted()) return

    const order = e.detail.order
    util.showModal('确认支付', `支付 ¥${((order.payAmount || order.price) / 100).toFixed(2)}`).then((confirm) => {
      if (!confirm) return
      util.showLoading('拉起支付...')
      orderApi.createPayOrder({ orderNo: order.orderNo })
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

  onRefundOrder(e) {
    const order = e.detail.order
    util.showModal('申请退款', '确认发起退款申请？').then((confirm) => {
      if (!confirm) return
      refundApi.applyRefund({
        orderNo: order.orderNo,
        refundReason: '用户申请退款'
      }).then(() => {
        util.showToast('退款申请已提交', 'success')
        this.setData({ currentTab: 'AFTER_SALE' }, () => {
          this.loadOrders()
        })
      }).catch((err) => {
        util.showToast(err && err.message ? err.message : '申请失败')
      })
    })
  },

  onRebuyOrder(e) {
    const order = e.detail.order
    util.navigateTo(`/pages/product-detail/product-detail?id=${order.productId}`)
  },

  goShopping() {
    util.switchTab('/pages/home/home')
  }
})
