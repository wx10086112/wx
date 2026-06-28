const util = require('../../utils/util')
const agreement = require('../../utils/agreement')
const orderApi = require('../../api/order')
const refundApi = require('../../api/refund')
const { toListThumbnailUrl } = require('../../utils/image-url')
const DEFAULT_PRODUCT_IMAGE = '/assets/images/merchant-logo-xiangyuan.png'

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

  normalizeOrderItem(item = {}, order = {}) {
    const price = Number(item.price || item.unitPrice || order.price || 0)
    const quantity = Math.max(1, Number(item.quantity || 1))
    return {
      ...item,
      productId: item.productId || item.id || order.productId,
      title: item.title || item.productName || item.name || order.title || order.productName || '',
      image: toListThumbnailUrl(item.image || item.coverImage || item.mainImage || order.image || DEFAULT_PRODUCT_IMAGE),
      quantity,
      price,
      priceText: (price / 100).toFixed(2),
      subtotalText: ((Number(item.subtotal || price * quantity)) / 100).toFixed(2)
    }
  },

  formatOrder(order = {}) {
    const meta = util.getOrderStatusMeta(order.status)
    const priceAmount = order.orderAmount || order.price || 0
    const payAmount = order.payAmount || order.price || 0
    const sourceItems = Array.isArray(order.items) && order.items.length
      ? order.items
      : [order]
    const items = sourceItems.map((item) => this.normalizeOrderItem(item, order))
    const firstItem = items[0] || {}
    const totalQuantity = items.reduce((sum, item) => sum + Number(item.quantity || 0), 0)
    const displayTitle = items.length > 1
      ? `${firstItem.title}等${items.length}件商品`
      : firstItem.title
    return {
      ...order,
      items,
      totalQuantity,
      itemCountText: items.length > 1 ? `共${items.length}种 ${totalQuantity}件` : `共${totalQuantity || 1}件`,
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
      image: firstItem.image || toListThumbnailUrl(order.image || order.coverImage || order.mainImage || DEFAULT_PRODUCT_IMAGE),
      title: displayTitle || order.title || order.productName || order.name || '',
      historyList: util.formatOrderHistory(order.history)
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
            util.showToast(err.message || err.msg || '支付失败，请重试')
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
    const firstItem = this.data.order.items && this.data.order.items[0]
    const productId = (firstItem && firstItem.productId) || this.data.order.productId
    if (!productId) return
    util.navigateTo(`/pages/product-detail/product-detail?id=${productId}`)
  },

  onShareAppMessage() {
    return {
      title: '订单详情',
      path: `/pages/order-detail/order-detail?orderNo=${this.data.orderNo}`
    }
  }
})
