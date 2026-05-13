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
          order: {
            ...response,
            statusMeta: util.getOrderStatusMeta(response.status),
            payAmountText: util.formatPrice(response.payAmount),
            payTimeText: util.formatDate(response.payTime || response.createTime),
            verifyTimeText: util.formatDate(response.verifyTime)
          }
        })
      })
      .catch(() => {
        const targetOrder = util.getOrderList().find((item) => item.orderNo === this.data.orderNo) || {}
        this.setData({
          order: {
            ...targetOrder,
            statusMeta: util.getOrderStatusMeta(targetOrder.status),
            payAmountText: util.formatPrice(targetOrder.payAmount),
            payTimeText: util.formatDate(targetOrder.payTime || targetOrder.createTime),
            verifyTimeText: util.formatDate(targetOrder.verifyTime)
          }
        })
      })
  },

  goVerify() {
    if (!app.needPermission(['verify.scan', 'verify.manual'])) return
    util.navigateTo(`/pages/verify/verify?orderNo=${this.data.orderNo}`)
  }
})
