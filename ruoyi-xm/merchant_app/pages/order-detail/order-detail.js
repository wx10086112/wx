const util = require('../../utils/util')
const api = require('../../api/index')

const app = getApp()

Page({
  data: {
    orderNo: '',
    order: {}
  },

  onLoad(options) {
    this.setData({
      orderNo: options.orderNo || ''
    })
  },

  onShow() {
    if (!app.needLogin()) return
    this.loadData()
  },

  loadData() {
    api
      .getMerchantOrderDetail(this.data.orderNo)
      .then((response) => {
        this.setData({
          order: this.buildOrderDisplay(response)
        })
      })
      .catch(() => {
        const targetOrder = util.getOrderList().find((item) => item.orderNo === this.data.orderNo) || {}
        this.setData({
          order: this.buildOrderDisplay(targetOrder)
        })
      })
  },

  buildOrderDisplay(order) {
    return {
      ...order,
      statusMeta: util.getOrderStatusMeta(order.status),
      payAmountText: util.formatPrice(order.payAmount),
      payTimeText: util.formatDate(order.payTime || order.createTime),
      createTimeText: util.formatDate(order.createTime),
      acceptTimeText: util.formatDate(order.acceptTime),
      shipTimeText: util.formatDate(order.shipTime),
      completeTimeText: util.formatDate(order.completeTime),
      verifyTimeText: util.formatDate(order.verifyTime),
      rejectTimeText: util.formatDate(order.rejectTime)
    }
  },

  goVerify() {
    if (!app.needPermission(['verify.scan', 'verify.manual'])) return
    util.navigateTo(`/pages/verify/verify?orderNo=${this.data.orderNo}`)
  },

  handleAcceptOrder() {
    if (!app.needPermission(['order.manage'])) return
    util.showModal('接单确认', '确定要接受该订单吗？').then((confirm) => {
      if (!confirm) return
      api.acceptMerchantOrder(this.data.orderNo)
        .then(() => {
          util.showToast('接单成功', 'success')
          this.loadData()
        })
        .catch(() => {
          const result = util.acceptOrder(this.data.orderNo)
          util.showToast(result.message, result.success ? 'success' : 'none')
          this.loadData()
        })
    })
  },

  handleRejectOrder() {
    if (!app.needPermission(['order.manage'])) return
    util.showModalWithInput('拒单原因', '请输入拒绝接单的原因').then((reason) => {
      if (reason === null) return
      api.rejectMerchantOrder(this.data.orderNo, { reason })
        .then(() => {
          util.showToast('已拒单', 'success')
          this.loadData()
        })
        .catch(() => {
          const result = util.rejectOrder(this.data.orderNo, reason)
          util.showToast(result.message, result.success ? 'success' : 'none')
          this.loadData()
        })
    })
  },

  handleShipOrder() {
    if (!app.needPermission(['order.manage'])) return
    util.showModal('发货确认', '确定标记为配送中吗？').then((confirm) => {
      if (!confirm) return
      api.shipMerchantOrder(this.data.orderNo)
        .then(() => {
          util.showToast('已发货', 'success')
          this.loadData()
        })
        .catch(() => {
          const result = util.shipOrder(this.data.orderNo)
          util.showToast(result.message, result.success ? 'success' : 'none')
          this.loadData()
        })
    })
  },

  handleCompleteOrder() {
    if (!app.needPermission(['order.manage'])) return
    util.showModal('完成确认', '确定订单已送达完成吗？').then((confirm) => {
      if (!confirm) return
      api.completeMerchantOrder(this.data.orderNo)
        .then(() => {
          util.showToast('订单已完成', 'success')
          this.loadData()
        })
        .catch(() => {
          const result = util.completeOrder(this.data.orderNo)
          util.showToast(result.message, result.success ? 'success' : 'none')
          this.loadData()
        })
    })
  },

  handleApproveRefund() {
    if (!app.needPermission(['order.manage'])) return
    util.showModal('退款审核', '确定同意该订单的退款申请吗？退款将原路返回给用户。').then((confirm) => {
      if (!confirm) return
      api.approveRefund(this.data.orderNo)
        .then(() => {
          util.showToast('已同意退款', 'success')
          this.loadData()
        })
        .catch(() => {
          const result = util.approveRefundOrder(this.data.orderNo)
          util.showToast(result.message, result.success ? 'success' : 'none')
          this.loadData()
        })
    })
  },

  handleRejectRefund() {
    if (!app.needPermission(['order.manage'])) return
    util.showModalWithInput('拒绝退款', '请输入拒绝退款的原因').then((reason) => {
      if (reason === null) return
      api.rejectRefund(this.data.orderNo, { reason })
        .then(() => {
          util.showToast('已拒绝退款', 'success')
          this.loadData()
        })
        .catch(() => {
          const result = util.rejectRefundOrder(this.data.orderNo, reason)
          util.showToast(result.message, result.success ? 'success' : 'none')
          this.loadData()
        })
    })
  },

  handleCancelOrder() {
    if (!app.needPermission(['order.manage'])) return
    util.showModalWithInput('取消订单', '请输入取消原因').then((reason) => {
      if (reason === null) return
      api.cancelMerchantOrder(this.data.orderNo, { reason })
        .then(() => {
          util.showToast('订单已取消', 'success')
          this.loadData()
        })
        .catch(() => {
          const result = util.cancelOrder(this.data.orderNo, reason)
          util.showToast(result.message, result.success ? 'success' : 'none')
          this.loadData()
        })
    })
  }
})

