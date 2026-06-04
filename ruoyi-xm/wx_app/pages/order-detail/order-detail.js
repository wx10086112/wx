const util = require('../../utils/util')
const agreement = require('../../utils/agreement')
const orderApi = require('../../api/order')
const refundApi = require('../../api/refund')

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
    if (!this.data.orderNo) {
      this.setData({ loading: false })
      util.showToast('订单不存在')
      return
    }
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
      payAmountText: (payAmount / 100).toFixed(2),
      image: order.image || order.coverImage || order.mainImage || '',
      title: order.title || order.productName || order.name || ''
    }
  },

  loadOrderDetail() {
    if (!this.data.orderNo) return
    this.setData({ loading: true })

    orderApi
      .getOrderDetail(this.data.orderNo)
      .then((res) => {
        const rawOrder = res.data || res || {}
        this.setData({
          order: this.formatOrder(rawOrder),
          loading: false
        })
      })
      .catch(() => {
        this.setData({ loading: false })
        util.showToast('订单加载失败')
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
        .catch((err) => {
          util.showToast(err && err.msg ? err.msg : '取消失败')
        })
    })
  },

  payOrder() {
    if (!agreement.assertAgreementAccepted()) return

    util.showModal('确认支付', `确认支付 ¥${this.data.order.payAmountText} 吗？`).then((confirm) => {
      if (!confirm) return
      util.showLoading('支付中...')
      orderApi
        .createPayOrder({ orderNo: this.data.orderNo })
        .then((res) => {
          const payParams = res.data || res
          if (payParams && payParams.timeStamp) {
            return util.requestPayment(payParams)
          }
          return Promise.reject(new Error('no pay params'))
        })
        .then(() => {
          util.hideLoading()
          util.showToast('支付成功', 'success')
          util.requestSubscribeMessage().then(() => {
            this.loadOrderDetail()
          })
        })
        .catch((err) => {
          util.hideLoading()
          if (err && err.message !== '用户取消支付') {
            util.showToast(err.msg || '支付失败，请重试')
          }
        })
    })
  },

  applyRefund() {
    util.showModal('申请退款', '确认申请退款？').then((confirm) => {
      if (!confirm) return
      refundApi
        .applyRefund({ orderNo: this.data.orderNo, refundReason: '用户申请退款' })
        .then(() => {
          util.showToast('退款申请已提交', 'success')
          this.loadOrderDetail()
        })
        .catch((err) => {
          util.showToast(err && err.msg ? err.msg : '申请失败')
        })
    })
  },

  contactMerchant() {
    const phone = this.data.order.merchantPhone
    if (phone) {
      wx.makePhoneCall({ phoneNumber: phone })
    } else {
      util.showToast('暂无联系方式')
    }
  },

  showCode() {
    if (this.data.order.writeOffCode) {
      util.showToast(`核销码：${this.data.order.writeOffCode}`)
    } else {
      util.showToast('暂无核销码')
    }
  },

  queryWriteOffResult() {
    this.loadOrderDetail()
  },

  buyAgain() {
    if (!this.data.order.productId) return
    util.navigateTo(`/pages/product-detail/product-detail?id=${this.data.order.productId}`)
  },

  onShareAppMessage() {
    return {
      title: '订单详情',
      path: `/pages/order-detail/order-detail?orderNo=${this.data.orderNo}`
    }
  }
})
