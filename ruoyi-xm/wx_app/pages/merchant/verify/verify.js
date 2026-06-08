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
      this.setData({
        manualCode: options.orderNo || ''
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
          recentRecordList: (response || []).slice(0, 5).map(this.buildRecentRecordDisplay)
        })
      })
      .catch(() => {
        this.setData({ recentRecordList: [] })
      })
  },

  buildRecentRecordDisplay(item) {
    const rawCode = item.writeOffCode || item.inputCode
    const displayCode = item.status === 'FAILED' && !item.orderNo ? '' : util.maskWriteOffCode(rawCode)
    return {
      ...item,
      displayCode,
      verifyTimeText: util.formatDate(item.verifyTime),
      payAmountText: util.formatPrice(item.payAmount),
      amountLabel: item.status === 'FAILED' ? '失败' : `¥${util.formatPrice(item.payAmount)}`
    }
  },

  handleCodeInput(e) {
    this.setData({
      manualCode: String(e.detail.value || '').trim().toUpperCase()
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
    this.processVerifyCode(String(this.data.manualCode || '').trim().toUpperCase())
  },

  processVerifyCode(code) {
    const normalizedCode = String(code || '').trim().toUpperCase()
    api
        .writeOffByCode(normalizedCode)
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
      .catch((err = {}) => {
        this.setData({ verifyResult: null })
        util.showToast(err.message || '核销失败，请重试')
        this.loadRecentRecords()
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
