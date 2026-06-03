const app = getApp()
const DEFAULT_BASE_URL = 'http://localhost:8080'
const BASE_URL_STORAGE_KEY = 'baseUrl'

const getBaseUrl = () => {
  const runtimeBaseUrl = app.baseUrl || wx.getStorageSync(BASE_URL_STORAGE_KEY) || DEFAULT_BASE_URL
  return String(runtimeBaseUrl).trim().replace(/\/+$/, '') || DEFAULT_BASE_URL
}
const getAppId = () => (app.globalData && app.globalData.appId) || ''

const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('token')

    const header = {
      'Content-Type': 'application/json',
      'X-Wx-AppId': getAppId(),
      ...options.header
    }

    if (token) {
      header['Wx-Authorization'] = token.startsWith('Bearer ') ? token : `Bearer ${token}`
    }

    wx.request({
      url: getBaseUrl() + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: header,
      success: (res) => {
        if (res.statusCode === 200) {
          if (res.data.code === 200 || res.data.code === 0) {
            resolve(res.data)
          } else if (res.data.code === 401) {
            app.clearLoginInfo()
            wx.showToast({
              title: '登录已过期，请重新登录',
              icon: 'none'
            })
            reject(new Error('登录已过期'))
          } else {
            wx.showToast({
              title: res.data.msg || '请求失败',
              icon: 'none'
            })
            reject(new Error(res.data.msg || '请求失败'))
          }
        } else {
          wx.showToast({
            title: '网络错误',
            icon: 'none'
          })
          reject(new Error('网络错误'))
        }
      },
      fail: (err) => {
        wx.showToast({
          title: '网络连接失败',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

const get = (url, data) => {
  return request({
    url,
    method: 'GET',
    data
  })
}

const post = (url, data) => {
  return request({
    url,
    method: 'POST',
    data
  })
}

const put = (url, data) => {
  return request({
    url,
    method: 'PUT',
    data
  })
}

const del = (url, data) => {
  return request({
    url,
    method: 'DELETE',
    data
  })
}

const uploadFile = (filePath, url = '/wxmini/common/upload') => {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('token')
    const header = {
      'X-Wx-AppId': getAppId()
    }
    if (token) {
      header['Wx-Authorization'] = token.startsWith('Bearer ') ? token : `Bearer ${token}`
    }

    wx.uploadFile({
      url: getBaseUrl() + url,
      filePath: filePath,
      name: 'file',
      header,
      success: (res) => {
        const data = JSON.parse(res.data)
        if (data.code === 200 || data.code === 0) {
          resolve(data)
        } else {
          reject(new Error(data.msg || '上传失败'))
        }
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

module.exports = {
  request,
  get,
  post,
  put,
  del,
  uploadFile,
  getBaseUrl
}
