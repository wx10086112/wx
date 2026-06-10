const merchantApi = require('../api/merchant')
const util = require('./util')

const getMerchantId = (merchant = {}) => Number(merchant.merchantId || merchant.id || 0)

const pickMerchantList = (response = {}) => {
  const payload = response.data || response
  if (Array.isArray(payload)) return payload
  if (Array.isArray(payload.rows)) return payload.rows
  if (Array.isArray(response.rows)) return response.rows
  if (Array.isArray(payload.list)) return payload.list
  return []
}

const buildEntry = (merchant = {}) => {
  const merchantId = getMerchantId(merchant)
  if (!merchantId) return null
  return {
    merchantId,
    merchantName: merchant.merchantName || merchant.name || merchant.storeName || '',
    contact: merchant.contact || merchant.contactName || '',
    phone: merchant.phone || merchant.contactPhone || '',
    loginPage: `/pages/merchant/login/login?merchantId=${merchantId}`
  }
}

const navigateMerchant = (entry) => {
  wx.navigateTo({
    url: entry.loginPage || `/pages/merchant/login/login?merchantId=${entry.merchantId}`
  })
}

const openMerchantPortal = () => {
  const app = getApp()
  let loadingVisible = false

  const showLoading = () => {
    if (loadingVisible) return
    util.showLoading('进入商家后台...')
    loadingVisible = true
  }

  const hideLoading = () => {
    if (!loadingVisible) return
    util.hideLoading()
    loadingVisible = false
  }

  if (app.globalData.isMerchantLoggedIn) {
    wx.navigateTo({
      url: '/pages/merchant/workbench/workbench'
    })
    return Promise.resolve()
  }

  const cachedEntry = app.getMerchantEntry ? app.getMerchantEntry() : null
  if (cachedEntry && cachedEntry.merchantId) {
    navigateMerchant(cachedEntry)
    return Promise.resolve()
  }

  showLoading()
  return merchantApi
    .getMerchantList({ pageNum: 1, pageSize: 1 })
    .then((response) => {
      const entry = buildEntry(pickMerchantList(response)[0])
      hideLoading()

      if (!entry) {
        util.showToast('请扫描后台生成的商家入口码')
        return
      }

      const normalizedEntry = app.setMerchantEntry ? app.setMerchantEntry(entry) : entry
      navigateMerchant(normalizedEntry || entry)
    })
    .catch((err) => {
      hideLoading()
      util.showToast((err && err.message) || '请扫描后台生成的商家入口码')
    })
}

module.exports = {
  openMerchantPortal
}
