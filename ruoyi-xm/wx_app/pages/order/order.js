const mock = require('../../data/mock')
const util = require('../../utils/util')
const orderApi = require('../../api/order')

Page({
  data: {
    tabs: [
      { label: '全部', value: 'ALL' },
      { label: '待支付', value: 'PENDING_PAY' },
      { label: '待使用', value: 'UNUSED' },
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
    this.ensureOrderStorage()
    this.loadOrders()
  },

  onShow() {
    this.ensureOrderStorage()
    this.loadOrders()
  },

  onPullDownRefresh() {
    this.loadOrders().then(() => {
      wx.stopPullDownRefresh()
    })
  },

  ensureOrderStorage() {
    const stored = util.getStoredOrderList()
    const validProductIds = mock.grouponList.map((item) => item.id)
    const hasLegacyStoreOrder = stored.some((item) => {
      return item.merchantId !== mock.merchantInfo.storeId || !validProductIds.includes(item.productId)
    })
    if (!stored.length || hasLegacyStoreOrder) {
      util.setStoredOrderList(mock.orderList)
    }
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
    if (tab === 'UNUSED') return list.filter((item) => item.status === 'PAID_UNUSED')
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
        const orders = (res || [])
          .sort((a, b) => (b.createTime || 0) - (a.createTime || 0))
          .map((item) => this.normalizeOrder(item))
        this.setData({
          orderStats: this.buildOrderStats(orders),
          orderList: orders,
          loading: false
        })
      })
      .catch(() => {
        this.loadOrdersLocal()
      })
  },

  loadOrdersLocal() {
    const orders = util
      .getStoredOrderList(mock.orderList)
      .sort((a, b) => (b.createTime || 0) - (a.createTime || 0))
      .map((item) => this.normalizeOrder(item))

    this.setData({
      orderStats: this.buildOrderStats(orders),
      orderList: this.getFilteredOrders(orders),
      loading: false
    })
  },

  updateOrderList(updateHandler, successText) {
    const orders = util.getStoredOrderList(mock.orderList)
    const nextOrders = updateHandler(orders)
    util.setStoredOrderList(nextOrders)
    if (successText) {
      util.showToast(successText, 'success')
    }
    this.loadOrders()
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
      this.updateOrderList(
        (orders) =>
          orders.map((item) =>
            item.orderNo === order.orderNo ? util.transitionOrderToCancelled(item) : item
          ),
        '订单已取消'
      )
    })
  },

  onPayOrder(e) {
    const order = e.detail.order
    util.showModal('确认支付', `支付 ¥${((order.payAmount || order.price) / 100).toFixed(2)} 后将生成核销码`).then((confirm) => {
      if (!confirm) return
      this.setData({ currentTab: 'UNUSED' }, () => {
        this.updateOrderList(
          (orders) => orders.map((item) => (item.orderNo === order.orderNo ? util.transitionOrderToPaidUnused(item) : item)),
          '支付成功'
        )
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
      this.setData({ currentTab: 'AFTER_SALE' }, () => {
        this.updateOrderList(
          (orders) => orders.map((item) => (item.orderNo === order.orderNo ? util.transitionOrderToRefunding(item) : item)),
          '退款申请已提交'
        )
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
