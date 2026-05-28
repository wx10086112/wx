const app = getApp()

const baseUrl = app.baseUrl || 'http://localhost:8080'

const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('token')
    
    const header = {
      'Content-Type': 'application/json',
      ...options.header
    }
    
    if (token) {
      header['Wx-Authorization'] = token
    }
    
    wx.request({
      url: baseUrl + options.url,
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
    
    wx.uploadFile({
      url: baseUrl + url,
      filePath: filePath,
      name: 'file',
      header: token ? { 'Wx-Authorization': token } : {},
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
  baseUrl
}
