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
        this.processVerifyCode(res.result || '')
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
    const normalizedCode = this.normalizeVerifyInput(code)
    if (!normalizedCode) {
      util.showToast('未识别到有效核销码')
      return
    }
    api
        .writeOffByCode(normalizedCode)
      .then((response) => {
        const result = response.data || response || {}
        this.setData({
          verifyResult: {
            ...result,
            payAmountText: util.formatPrice(result.payAmount),
            verifyTimeText: util.formatDate(result.verifyTime)
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

  normalizeVerifyInput(rawValue) {
    let value = String(rawValue || '').trim()
    if (!value) return ''

    value = this.safeDecode(value)
    const jsonValue = this.extractFromJson(value)
    if (jsonValue) value = jsonValue

    const queryValue = this.extractFromQuery(value)
    if (queryValue) value = queryValue

    value = this.safeDecode(value)
    const compact = String(value || '').replace(/\s+/g, '').toUpperCase()
    const codeMatch = compact.match(/LY\d{8}[23456789ABCDEFGHJKLMNPQRSTUVWXYZ]{8}/)
    if (codeMatch) return codeMatch[0]
    const orderMatch = compact.match(/(?:ORD|M)\d{8,24}/)
    if (orderMatch) return orderMatch[0]
    return compact
  },

  safeDecode(value) {
    let result = String(value || '').trim()
    for (let i = 0; i < 2; i += 1) {
      try {
        const decoded = decodeURIComponent(result)
        if (decoded === result) break
        result = decoded
      } catch (e) {
        break
      }
    }
    return result
  },

  extractFromJson(value) {
    const text = String(value || '').trim()
    if (!text || (text[0] !== '{' && text[0] !== '[')) return ''
    try {
      const parsed = JSON.parse(text)
      const source = Array.isArray(parsed) ? parsed[0] : parsed
      return (source && (source.writeOffCode || source.verifyCode || source.code || source.orderNo || source.scene)) || ''
    } catch (e) {
      return ''
    }
  },

  extractFromQuery(value) {
    const text = String(value || '')
    const queryText = text.includes('?') ? text.split('?').slice(1).join('?') : text
    const pairs = queryText.split(/[&?#]/)
    const keys = ['writeOffCode', 'verifyCode', 'code', 'orderNo', 'scene']
    for (const pair of pairs) {
      const index = pair.indexOf('=')
      if (index <= 0) continue
      const key = pair.slice(0, index)
      const val = pair.slice(index + 1)
      if (keys.some((item) => item.toLowerCase() === key.toLowerCase())) {
        return this.safeDecode(val)
      }
    }
    return ''
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
