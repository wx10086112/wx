const PROD_BASE_URL = 'https://ld-console.lingdian.site/prod-api'
const BASE_URL_STORAGE_KEY = 'baseUrl'
const { normalizeImageFields } = require('./image-url')

const getDefaultBaseUrl = () => {
  return PROD_BASE_URL
}

const normalizeBaseUrl = (baseUrl = getDefaultBaseUrl()) => {
  const normalized = String(baseUrl || getDefaultBaseUrl()).trim().replace(/\/+$/, '')
  const unsafe = !/^https:\/\//i.test(normalized) ||
    /^https?:\/\/(localhost|127\.|0\.0\.0\.0|10\.|192\.168\.|172\.(1[6-9]|2\d|3[0-1])\.)/i.test(normalized) ||
    /(example|invalid|placeholder|xxx)/i.test(normalized)
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

const clearLoginInfo = () => {
  const app = getAppInstance()
  if (app && app.clearLoginInfo) {
    app.clearLoginInfo()
  }
}

const parseUploadResponse = (rawData = '') => {
  try {
    return JSON.parse(rawData || '{}')
  } catch (e) {
    return null
  }
}

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
          const responseData = normalizeImageFields(res.data || {})
          if (responseData.code === 200 || responseData.code === 0) {
            resolve(responseData)
          } else if (responseData.code === 401) {
            clearLoginInfo()
            wx.showToast({
              title: '登录已过期，请重新登录',
              icon: 'none'
            })
            reject(new Error('登录已过期'))
          } else {
            wx.showToast({
              title: responseData.msg || '请求失败',
              icon: 'none'
            })
            reject(new Error(responseData.msg || '请求失败'))
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
        if (res.statusCode === 401) {
          clearLoginInfo()
          reject(new Error('登录已过期'))
          return
        }
        if (res.statusCode < 200 || res.statusCode >= 300) {
          reject(new Error('上传失败'))
          return
        }

        const data = parseUploadResponse(res.data)
        if (!data) {
          reject(new Error('上传返回异常'))
          return
        }
        const responseData = normalizeImageFields(data)
        if (responseData.code === 200 || responseData.code === 0) {
          resolve(responseData)
        } else {
          reject(new Error(responseData.msg || '上传失败'))
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
