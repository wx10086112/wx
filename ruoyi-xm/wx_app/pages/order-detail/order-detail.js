const mock = require('../../data/mock')
const util = require('../../utils/util')

Page({
  data: {
    orderNo: '',
    order: {},
    loading: true
  },

  onLoad(options) {
    this.setData({
      orderNo: options.orderNo || mock.orderList[0].orderNo
    })
    this.loadOrderDetail()
  },

  onShow() {
    this.loadOrderDetail()
  },

  formatOrder(order = {}) {
    const meta = util.getOrderStatusMeta(order.status)
    const priceAmount = order.orderAmount || order.price || 0
    const payAmount = order.payAmount || order.price || 0
    return {
      ...order,
      statusText: meta.text,
      statusClass: order.status || '',
      statusIcon: meta.icon,
      createTimeText: order.createTime ? util.formatDate(order.createTime, 'YYYY-MM-DD HH:mm') : '',
      payTimeText: order.payTime ? util.formatDate(order.payTime, 'YYYY-MM-DD HH:mm') : '',
      writeOffTimeText: order.writeOffTime ? util.formatDate(order.writeOffTime, 'YYYY-MM-DD HH:mm') : '',
      refundTimeText: order.refundTime ? util.formatDate(order.refundTime, 'YYYY-MM-DD HH:mm') : '',
      writeOffDeadlineText: order.writeOffDeadline ? util.formatDate(order.writeOffDeadline, 'YYYY-MM-DD HH:mm') : '',
      priceAmountText: (priceAmount / 100).toFixed(2),
      couponAmountText: ((order.couponAmount || 0) / 100).toFixed(2),
      payAmountText: (payAmount / 100).toFixed(2)
    }
  },

  loadOrderDetail() {
    this.setData({ loading: true })
    setTimeout(() => {
      const rawOrder = util.getStoredOrderList(mock.orderList).find((item) => item.orderNo === this.data.orderNo) || {}
      this.setData({
        order: this.formatOrder(rawOrder),
        loading: false
      })
    }, 120)
  },

  updateOrder(updateHandler, successText) {
    const orders = util.getStoredOrderList(mock.orderList)
    const nextOrders = updateHandler(orders)
    util.setStoredOrderList(nextOrders)
    if (successText) {
      util.showToast(successText, 'success')
    }
    this.loadOrderDetail()
  },

  cancelOrder() {
    util.showModal('取消订单', '确认取消当前订单？').then((confirm) => {
      if (!confirm) return
      this.updateOrder(
        (orders) =>
          orders.map((item) => (item.orderNo === this.data.orderNo ? util.transitionOrderToCancelled(item) : item)),
        '已取消'
      )
    })
  },

  payOrder() {
    util.showModal('确认支付', `确认支付 ¥${this.data.order.payAmountText} 吗？`).then((confirm) => {
      if (!confirm) return
      this.updateOrder(
        (orders) =>
          orders.map((item) => (item.orderNo === this.data.orderNo ? util.transitionOrderToPaidUnused(item) : item)),
        '支付成功'
      )
    })
  },

  applyRefund() {
    util.showModal('申请退款', '确认申请退款？').then((confirm) => {
      if (!confirm) return
      this.updateOrder(
        (orders) =>
          orders.map((item) => (item.orderNo === this.data.orderNo ? util.transitionOrderToRefunding(item) : item)),
        '退款申请已提交'
      )
    })
  },

  contactMerchant() {
    util.showToast('已唤起联系门店能力')
  },

  showCode() {
    util.showToast('请向门店出示核销码')
  },

  queryWriteOffResult() {
    util.showToast('暂无新的核销记录')
  },

  buyAgain() {
    if (!this.data.order.productId) return
    util.navigateTo(`/pages/product-detail/product-detail?id=${this.data.order.productId}`)
  },

  goReview() {
    util.navigateTo(`/pages/review/review-create?orderNo=${this.data.orderNo}`)
  },

  onShareAppMessage() {
    return {
      title: '订单详情',
      path: `/pages/order-detail/order-detail?orderNo=${this.data.orderNo}`
    }
  }
})
