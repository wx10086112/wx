const util = require('../../../utils/merchant-util')
const api = require('../../../api/merchant-mini/index')

const app = getApp()

Page({
  data: {
    manualCode: '',
    orderHint: {},
    verifyResult: null,
    recentRecordList: [],
    merchantNavList: util.getMerchantNavList('verify')
  },

  onLoad(options) {
    if (options.orderNo) {
      const hintOrder = util.getOrderList().find((item) => item.orderNo === options.orderNo) || {}
      this.setData({
        orderHint: hintOrder,
        manualCode: hintOrder.writeOffCode || ''
      })
    }
  },

  onShow() {
    if (!app.needMerchantLogin()) return
    this.loadRecentRecords()
  },

  loadRecentRecords() {
    api
      .getVerifyRecordList()
      .then((response) => {
        this.setData({
          recentRecordList: (response || []).slice(0, 5).map((item) => ({
            ...item,
            verifyTimeText: util.formatDate(item.verifyTime),
            payAmountText: util.formatPrice(item.payAmount),
            amountLabel: item.status === 'FAILED' ? '失败' : `¥${util.formatPrice(item.payAmount)}`
          }))
        })
      })
      .catch(() => {
        const recentRecordList = util
          .getVerifyRecordList()
          .sort((a, b) => (b.verifyTime || 0) - (a.verifyTime || 0))
          .slice(0, 5)
          .map((item) => ({
            ...item,
            verifyTimeText: util.formatDate(item.verifyTime),
            payAmountText: util.formatPrice(item.payAmount),
            amountLabel: item.status === 'FAILED' ? '失败' : `¥${util.formatPrice(item.payAmount)}`
          }))

        this.setData({ recentRecordList })
      })
  },

  handleCodeInput(e) {
    this.setData({
      manualCode: e.detail.value.trim()
    })
  },

  scanCode() {
    if (!app.needPermission(['verify.scan'])) return
    wx.scanCode({
      success: (res) => {
        this.processVerifyCode((res.result || '').trim())
      },
      fail: () => {
        util.showToast('未完成扫码，请改用手动输入')
      }
    })
  },

  submitManualVerify() {
    if (!app.needPermission(['verify.manual'])) return
    if (!this.data.manualCode) {
      util.showToast('请输入核销码')
      return
    }
    this.processVerifyCode(this.data.manualCode)
  },

  processVerifyCode(code) {
    api
      .writeOffByCode(code)
      .then((response) => {
        this.setData({
          verifyResult: {
            ...response,
            payAmountText: util.formatPrice(response.payAmount),
            verifyTimeText: util.formatDate(response.verifyTime)
          },
          manualCode: ''
        })
        util.showToast('核销成功', 'success')
        this.loadRecentRecords()
      })
      .catch(() => {
        const result = util.verifyOrderByCode(code, app.globalData.staffUser || {})
        this.setData({
          verifyResult: result.order
            ? {
                ...result.order,
                payAmountText: util.formatPrice(result.order.payAmount),
                verifyTimeText: util.formatDate(result.order.verifyTime)
              }
            : null
        })
        util.showToast(result.message, result.success ? 'success' : 'none')
        if (result.success) {
          this.loadRecentRecords()
          this.setData({
            manualCode: ''
          })
        } else {
          this.loadRecentRecords()
        }
      })
  },

  goVerifyRecords() {
    if (!app.needPermission(['verify.record', 'verify.scan', 'verify.manual'])) return
    util.navigateTo('/pages/merchant/verify-records/verify-records')
  },

  goMerchantTab(e) {
    const { url } = e.currentTarget.dataset
    if (url) {
      util.openMerchantMainPage(url)
    }
  }
})
