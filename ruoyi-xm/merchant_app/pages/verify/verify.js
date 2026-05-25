const util = require('../../utils/util')
const api = require('../../api/index')

const app = getApp()

Page({
  data: {
    manualCode: '',
    orderHint: {},
    verifyResult: null,
    recentRecordList: []
  },

  onLoad(options) {
    if (options.orderNo) {
      this.setData({
        'orderHint.orderNo': options.orderNo
      })
    }
  },

  onShow() {
    if (!app.needLogin()) return
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
        this.setData({ recentRecordList: [] })
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
        util.showToast('核销失败，请重试')
      })
  },

  goVerifyRecords() {
    if (!app.needPermission(['verify.record', 'verify.scan', 'verify.manual'])) return
    util.navigateTo('/pages/verify-records/verify-records')
  }
})
