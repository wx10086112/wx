const { get, post, put } = require('../utils/request')
const app = getApp()

const getMerchantGoodsList = (data = {}) => get('/wxmini/merchant-mini/goods/list', data)
const saveMerchantGoods = (data) => post('/wxmini/merchant-mini/goods/save', data)
const updateMerchantGoodsStatus = (data) => put('/wxmini/merchant-mini/goods/status', data)
const batchUpdateGoodsStatus = (goodsIds, status) => put('/wxmini/merchant-mini/goods/batch-status', { goodsIds, status })
const uploadMerchantGoodsImage = (filePath) => {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('merchantToken')
    wx.uploadFile({
      url: `${app.baseUrl || 'http://localhost:8080'}/wxmini/merchant-mini/goods/image/upload`,
      filePath,
      name: 'file',
      header: token ? { 'Wx-Authorization': `Bearer ${token}` } : {},
      success: (res) => {
        if (res.statusCode !== 200) {
          reject(new Error('图片上传失败'))
          return
        }
        const responseData = JSON.parse(res.data || '{}')
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
  batchUpdateGoodsStatus,
  uploadMerchantGoodsImage
}
