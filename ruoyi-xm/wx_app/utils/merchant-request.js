const app = getApp()

const getBaseUrl = () => app.baseUrl || 'http://localhost:8080'
const getAppId = () => (app.globalData && app.globalData.appId) || ''
const getMerchantEntry = () => (app.getMerchantEntry ? app.getMerchantEntry() : null)

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

        const responseData = res.data || {}
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
  put
}
