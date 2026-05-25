const util = require('../../utils/util')
const orderApi = require('../../api/order')

Page({
  data: {
    orderNo: '',
    order: {},
    loading: true
  },

  onLoad(options) {
    this.setData({
      orderNo: options.orderNo || ''
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

    orderApi
      .getOrderDetail(this.data.orderNo)
      .then((res) => {
        this.setData({
          order: this.formatOrder(res || {}),
          loading: false
        })
      })
      .catch(() => {
        this.setData({
          order: {},
          loading: false
        })
        util.showToast('加载失败，请重试')
      })
  },

  cancelOrder() {
    util.showModal('取消订单', '确认取消当前订单？').then((confirm) => {
      if (!confirm) return
      orderApi.cancelOrder(this.data.orderNo)
        .then(() => {
          util.showToast('已取消', 'success')
          this.loadOrderDetail()
        })
        .catch(() => {
          util.showToast('操作失败，请重试')
        })
    })
  },

  payOrder() {
    util.showModal('确认支付', `确认支付 ¥${this.data.order.payAmountText} 吗？`).then((confirm) => {
      if (!confirm) return
      util.showLoading('支付中...')
      orderApi
        .createPayOrder({ orderNo: this.data.orderNo })
        .then((res) => {
          const payParams = res
          if (payParams && payParams.timeStamp) {
            return util.requestPayment(payParams)
          }
          return Promise.reject(new Error('no pay params'))
        })
        .then(() => {
          util.hideLoading()
          util.showToast('支付成功', 'success')
          util.requestSubscribeMessage()
          this.loadOrderDetail()
        })
        .catch((err) => {
          util.hideLoading()
          if (err && err.message !== '用户取消支付') {
            util.showToast('支付失败，请重试')
          }
        })
    })
  },

  applyRefund() {
    util.showModal('申请退款', '确认申请退款？').then((confirm) => {
      if (!confirm) return
      util.showToast('退款功能需要后端支持')
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
    util.navigateTo(`/pages/review/review?orderNo=${this.data.orderNo}`)
  },

  onShareAppMessage() {
    return {
      title: '订单详情',
      path: `/pages/order-detail/order-detail?orderNo=${this.data.orderNo}`
    }
  }
})
