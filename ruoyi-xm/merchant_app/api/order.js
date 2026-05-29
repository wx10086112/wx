const { get, post } = require('../utils/request')

const getMerchantOrderList = (data = {}) => get('/wxmini/merchant-mini/order/list', data)
const getMerchantOrderDetail = (orderNo) => get(`/wxmini/merchant-mini/order/detail/${orderNo}`)
const writeOffByCode = (code) => post(`/wxmini/merchant-mini/order/write-off/${code}`)
const cancelMerchantOrder = (orderNo, data) => post(`/wxmini/merchant-mini/order/cancel/${orderNo}`, data)
const approveRefund = (orderNo) => post(`/wxmini/merchant-mini/order/refund/approve/${orderNo}`)
const rejectRefund = (orderNo, data) => post(`/wxmini/merchant-mini/order/refund/reject/${orderNo}`, data)

module.exports = {
  getMerchantOrderList,
  getMerchantOrderDetail,
  writeOffByCode,
  cancelMerchantOrder,
  approveRefund,
  rejectRefund
}
