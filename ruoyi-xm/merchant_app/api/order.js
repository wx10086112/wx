const { get, post } = require('../utils/request')

const getMerchantOrderList = (data = {}) => get('/wxmini/merchant-mini/order/list', data)
const getMerchantOrderDetail = (orderNo) => get(`/wxmini/merchant-mini/order/detail/${orderNo}`)
const writeOffByCode = (code) => post(`/wxmini/merchant-mini/order/write-off/${code}`)

module.exports = {
  getMerchantOrderList,
  getMerchantOrderDetail,
  writeOffByCode
}
