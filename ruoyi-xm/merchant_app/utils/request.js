const app = getApp()

const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('merchantToken')
    const header = {
      'Content-Type': 'application/json',
      'X-Merchant-AppId': app.appId || '',
      ...options.header
    }

    if (token) {
      header['Wx-Authorization'] = `Bearer ${token}`
    }

    wx.request({
      url: `${app.baseUrl || 'http://localhost:8080'}${options.url}`,
      method: options.method || 'GET',
      data: options.data || {},
      header,
      success: (res) => {
        if (res.statusCode === 401) {
          app.clearLoginInfo()
          wx.redirectTo({ url: '/pages/login/login' })
          reject(new Error('登录已过期'))
          return
        }
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
