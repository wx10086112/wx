const { get, post, put, getBaseUrl } = require('../../utils/merchant-request')
const app = getApp()

const getMerchantGoodsList = (data = {}) => get('/wxmini/merchant-mini/goods/list', data)
const saveMerchantGoods = (data) => post('/wxmini/merchant-mini/goods/save', data)
const updateMerchantGoodsStatus = (data) => put('/wxmini/merchant-mini/goods/status', data)

const parseUploadResponse = (rawData = '') => {
  try {
    return JSON.parse(rawData || '{}')
  } catch (e) {
    return null
  }
}

const uploadMerchantGoodsImage = (filePath) => {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('merchantToken')
    const appId = (app.globalData && app.globalData.appId) || ''
    const header = {
      'X-Merchant-AppId': appId
    }
    if (token) {
      header['Wx-Authorization'] = `Bearer ${token}`
    }
    wx.uploadFile({
      url: `${getBaseUrl()}/wxmini/merchant-mini/goods/image/upload`,
      filePath,
      name: 'file',
      header,
      success: (res) => {
        if (res.statusCode === 401 || res.statusCode === 403) {
          reject(new Error('当前账号没有上传权限'))
          return
        }
        if (res.statusCode < 200 || res.statusCode >= 300) {
          reject(new Error('图片上传失败，请稍后重试'))
          return
        }

        const responseData = parseUploadResponse(res.data)
        if (!responseData) {
          reject(new Error('图片上传返回异常'))
          return
        }

        if (responseData.code === 200 || responseData.code === 0) {
          resolve(responseData.data !== undefined ? responseData.data : responseData)
          return
        }
        reject(new Error(responseData.msg || '图片上传失败'))
      },
      fail: reject
    })
  })
}

module.exports = {
  getMerchantGoodsList,
  saveMerchantGoods,
  updateMerchantGoodsStatus,
  uploadMerchantGoodsImage
}
