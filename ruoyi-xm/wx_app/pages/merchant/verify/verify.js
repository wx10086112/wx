const util = require('../../../utils/merchant-util')
const api = require('../../../api/merchant-mini/index')

const app = getApp()
const QUERY_CODE_KEYS = ['writeOffCode', 'verifyCode', 'code', 'orderNo', 'scene']

const copyPlainObject = (source) => {
  const target = {}
  if (!source) return target
  Object.keys(source).forEach((key) => {
    target[key] = source[key]
  })
  return target
}

const getDatasetValue = (event, key) => {
  const dataset = event && event.currentTarget ? event.currentTarget.dataset || {} : {}
  return dataset[key] || ''
}

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
        const recordList = Array.isArray(response) ? response : []
        this.setData({
          recentRecordList: recordList.slice(0, 5).map(this.buildRecentRecordDisplay)
        })
      })
      .catch(() => {
        this.setData({ recentRecordList: [] })
      })
  },

  buildRecentRecordDisplay(item) {
    const source = item || {}
    const rawCode = source.writeOffCode || source.inputCode
    const displayCode = source.status === 'FAILED' && !source.orderNo ? '' : util.maskWriteOffCode(rawCode)
    const result = copyPlainObject(source)
    result.displayCode = displayCode
    result.verifyTimeText = util.formatDate(source.verifyTime)
    result.payAmountText = util.formatPrice(source.payAmount)
    result.amountLabel = source.status === 'FAILED' ? '失败' : '¥' + util.formatPrice(source.payAmount)
    return result
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
        const verifyResult = copyPlainObject(result)
        verifyResult.payAmountText = util.formatPrice(result.payAmount)
        verifyResult.verifyTimeText = util.formatDate(result.verifyTime)
        this.setData({
          verifyResult,
          manualCode: ''
        })
        util.showToast('核销成功', 'success')
        this.loadRecentRecords()
      })
      .catch((err) => {
        const error = err || {}
        this.setData({ verifyResult: null })
        util.showToast(error.message || '核销失败，请重试')
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
    for (let i = 0; i < pairs.length; i += 1) {
      const pair = pairs[i]
      const index = pair.indexOf('=')
      if (index <= 0) continue
      const key = pair.slice(0, index)
      const val = pair.slice(index + 1)
      if (this.isVerifyQueryKey(key)) {
        return this.safeDecode(val)
      }
    }
    return ''
  },

  isVerifyQueryKey(key) {
    const normalizedKey = String(key || '').toLowerCase()
    for (let i = 0; i < QUERY_CODE_KEYS.length; i += 1) {
      if (QUERY_CODE_KEYS[i].toLowerCase() === normalizedKey) return true
    }
    return false
  },

  goVerifyRecords() {
    if (!app.needPermission(['verify.record', 'verify.scan', 'verify.manual'])) return
    util.navigateTo('/pages/merchant/verify-records/verify-records')
  },

  goMerchantTab(e) {
    const url = getDatasetValue(e, 'url')
    if (url) {
      util.openMerchantMainPage(url)
    }
  }
})
