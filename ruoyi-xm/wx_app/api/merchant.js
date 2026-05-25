const { get, post } = require('./request')

const getMerchantList = (data = {}) => {
  return get('/wxmini/merchant/list', data)
}

const getMerchantDetail = (id) => {
  return get(`/wxmini/merchant/detail/${id}`)
}

const getMerchantAlbum = (merchantId) => {
  return get(`/wxmini/merchant/album/${merchantId}`)
}

const getMerchantHome = (appid) => {
  return get('/wxmini/merchant/home', { appid })
}

module.exports = {
  getMerchantList,
  getMerchantDetail,
  getMerchantAlbum,
  getMerchantHome
}
