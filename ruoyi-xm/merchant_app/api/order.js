const { get, post } = require('../utils/request')

const getMerchantOrderList = (data = {}) => get('/wxmini/merchant-mini/order/list', data)
const getMerchantOrderDetail = (orderNo) => get(`/wxmini/merchant-mini/order/detail/${orderNo}`)
const writeOffByCode = (code) => post(`/wxmini/merchant-mini/order/write-off/${code}`)
const acceptMerchantOrder = (orderNo) => post(`/wxmini/merchant-mini/order/accept/${orderNo}`)
const rejectMerchantOrder = (orderNo, data) => post(`/wxmini/merchant-mini/order/reject/${orderNo}`, data)
const shipMerchantOrder = (orderNo) => post(`/wxmini/merchant-mini/order/ship/${orderNo}`)
const completeMerchantOrder = (orderNo) => post(`/wxmini/merchant-mini/order/complete/${orderNo}`)
const cancelMerchantOrder = (orderNo, data) => post(`/wxmini/merchant-mini/order/cancel/${orderNo}`, data)
const approveRefund = (orderNo) => post(`/wxmini/merchant-mini/order/refund/approve/${orderNo}`)
const rejectRefund = (orderNo, data) => post(`/wxmini/merchant-mini/order/refund/reject/${orderNo}`, data)

module.exports = {
  getMerchantOrderList,
  getMerchantOrderDetail,
  writeOffByCode,
  acceptMerchantOrder,
  rejectMerchantOrder,
  shipMerchantOrder,
  completeMerchantOrder,
  cancelMerchantOrder,
  approveRefund,
  rejectRefund
}
