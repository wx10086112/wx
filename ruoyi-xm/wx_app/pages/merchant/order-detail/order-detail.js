const util = require('../../../utils/merchant-util')
const api = require('../../../api/merchant-mini/index')

const app = getApp()

Page({
  data: {
    orderNo: '',
    order: {},
    cancelModalVisible: false,
    cancelReason: '',
    approveRefundModalVisible: false,
    rejectRefundModalVisible: false,
    rejectRefundReason: ''
  },

  onLoad(options) {
    this.setData({
      orderNo: options.orderNo || ''
    })
  },

  onShow() {
    if (!app.needMerchantLogin()) return
    this.loadData()
  },

  loadData() {
    api
      .getMerchantOrderDetail(this.data.orderNo)
      .then((response) => {
        const order = util.normalizeGrouponOrders(response ? [response] : [])[0] || {}
        this.setData({
          order: this.buildOrderDisplay(order)
        })
      })
      .catch((err = {}) => {
        this.setData({
          order: this.buildOrderDisplay({})
        })
        util.showToast(err.message || '订单详情加载失败')
      })
  },

  buildOrderDisplay(order) {
    const customerName = String(order.customerName || '').trim()
    const customerPhone = String(order.customerPhone || '').trim()
    return {
      ...order,
      statusMeta: util.getOrderStatusMeta(order.status),
      writeOffCodeMasked: util.maskWriteOffCode(order.writeOffCode),
      customerNameText: customerName || '-',
      customerPhoneText: customerPhone || '-',
      callPhone: util.getCallablePhone(customerPhone),
      payAmountText: util.formatPrice(order.payAmount),
      payTimeText: util.formatDate(order.payTime || order.createTime),
      createTimeText: util.formatDate(order.createTime),
      verifyTimeText: util.formatDate(order.verifyTime),
      refundTimeText: util.formatDate(order.refundTime),
      refundRejectTimeText: util.formatDate(order.refundRejectTime),
      cancelTimeText: util.formatDate(order.cancelTime)
    }
  },

  goVerify() {
    if (!app.needPermission(['verify.scan', 'verify.manual'])) return
    util.navigateTo(`/pages/merchant/verify/verify?orderNo=${this.data.orderNo}`)
  },

  callCustomer() {
    const phoneNumber = this.data.order.callPhone
    if (!phoneNumber) {
      util.showToast('暂无可拨打电话')
      return
    }
    wx.makePhoneCall({ phoneNumber })
  },

  handleApproveRefund() {
    if (!app.needPermission(['order.manage'])) return
    this.setData({ approveRefundModalVisible: true })
  },

  closeApproveRefundModal() {
    this.setData({ approveRefundModalVisible: false })
  },

  confirmApproveRefund() {
    api.approveRefund(this.data.orderNo)
      .then(() => {
        util.showToast('已同意退款', 'success')
        this.closeApproveRefundModal()
        this.loadData()
      })
      .catch((err = {}) => {
        util.showToast(err.message || '退款处理失败，请重试')
      })
  },

  handleRejectRefund() {
    if (!app.needPermission(['order.manage'])) return
    this.setData({
      rejectRefundModalVisible: true,
      rejectRefundReason: ''
    })
  },

  handleRejectRefundReasonInput(e) {
    this.setData({
      rejectRefundReason: e.detail.value
    })
  },

  closeRejectRefundModal() {
    this.setData({
      rejectRefundModalVisible: false,
      rejectRefundReason: ''
    })
  },

  confirmRejectRefund() {
    const reason = this.data.rejectRefundReason.trim()
    api.rejectRefund(this.data.orderNo, { reason })
      .then(() => {
        util.showToast('已拒绝退款', 'success')
        this.closeRejectRefundModal()
        this.loadData()
      })
      .catch((err = {}) => {
        util.showToast(err.message || '退款处理失败，请重试')
      })
  },

  handleCancelOrder() {
    if (!app.needPermission(['order.manage'])) return
    this.setData({
      cancelModalVisible: true,
      cancelReason: ''
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
      cancelReason: ''
    })
  },

  confirmCancelOrder() {
    const reason = this.data.cancelReason.trim()
    api.cancelMerchantOrder(this.data.orderNo, { reason })
      .then(() => {
        util.showToast('订单已取消', 'success')
        this.closeCancelModal()
        this.loadData()
      })
      .catch((err = {}) => {
        util.showToast(err.message || '订单取消失败，请重试')
      })
  }
})

