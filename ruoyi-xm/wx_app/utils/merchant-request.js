const PROD_BASE_URL = 'https://ld-console.lingdian.site/prod-api'
const DEV_BASE_URL = 'http://127.0.0.1:8080'
const BASE_URL_STORAGE_KEY = 'baseUrl'
const { normalizeImageFields } = require('./image-url')

const getEnvVersion = () => {
  try {
    const accountInfo = wx.getAccountInfoSync()
    return (accountInfo && accountInfo.miniProgram && accountInfo.miniProgram.envVersion) || 'release'
  } catch (e) {
    return 'release'
  }
}

const getDefaultBaseUrl = () => {
  return getEnvVersion() === 'develop' ? DEV_BASE_URL : PROD_BASE_URL
}

const normalizeBaseUrl = (baseUrl = getDefaultBaseUrl()) => {
  const normalized = String(baseUrl || getDefaultBaseUrl()).trim().replace(/\/+$/, '')
  const allowLocal = getEnvVersion() === 'develop' &&
    /^https?:\/\/(localhost|127\.|0\.0\.0\.0|10\.|192\.168\.|172\.(1[6-9]|2\d|3[0-1])\.)/i.test(normalized)
  const unsafe = !allowLocal && (!/^https:\/\//i.test(normalized) ||
    /^https?:\/\/(localhost|127\.|0\.0\.0\.0|10\.|192\.168\.|172\.(1[6-9]|2\d|3[0-1])\.)/i.test(normalized) ||
    /(example|invalid|placeholder|xxx)/i.test(normalized))
  return unsafe ? getDefaultBaseUrl() : normalized
}

const getAppInstance = () => {
  try {
    return getApp()
  } catch (e) {
    return null
  }
}

const getBaseUrl = () => {
  const app = getAppInstance()
  const runtimeBaseUrl = (app && app.baseUrl) || wx.getStorageSync(BASE_URL_STORAGE_KEY) || getDefaultBaseUrl()
  return normalizeBaseUrl(runtimeBaseUrl)
}
const getAppId = () => {
  const app = getAppInstance()
  return (app && app.globalData && app.globalData.appId) || ''
}
const getMerchantEntry = () => {
  const app = getAppInstance()
  return app && app.getMerchantEntry ? app.getMerchantEntry() : null
}

const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('merchantToken')
    const merchantEntry = getMerchantEntry()
    const header = {
      'Content-Type': 'application/json',
      'X-Merchant-AppId': getAppId(),
      ...options.header
    }

    if (merchantEntry && merchantEntry.merchantId) {
      header['X-Merchant-Id'] = String(merchantEntry.merchantId)
    }

    if (token) {
      header['Wx-Authorization'] = `Bearer ${token}`
    }

    wx.request({
      url: `${getBaseUrl()}${options.url}`,
      method: options.method || 'GET',
      data: options.data || {},
      header,
      success: (res) => {
        if (res.statusCode !== 200) {
          reject(new Error('网络请求失败'))
          return
        }

        const responseData = normalizeImageFields(res.data || {})
        if (responseData.code === 200 || responseData.code === 0) {
          resolve(responseData.data !== undefined ? responseData.data : responseData)
          return
        }

        reject(new Error(responseData.msg || '请求失败'))
      },
      fail: (err) => reject(err)
    })
  })
}

const get = (url, data) => request({ url, method: 'GET', data })
const post = (url, data) => request({ url, method: 'POST', data })
const put = (url, data) => request({ url, method: 'PUT', data })

module.exports = {
  request,
  get,
  post,
  put,
  getBaseUrl
}
