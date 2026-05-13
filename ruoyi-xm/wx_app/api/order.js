const { get, post } = require('./request')

const getOrderList = (data = {}) => {
  return get('/wxmini/order/list', data)
}

const getOrderDetail = (id) => {
  return get(`/wxmini/order/detail/${id}`)
}

const createOrder = (data) => {
  return post('/wxmini/order/create', data)
}

const cancelOrder = (orderNo) => {
  return post(`/wxmini/order/cancel/${orderNo}`)
}

const writeOffOrder = (code) => {
  return post(`/wxmini/order/writeOff/${code}`)
}

const createPayOrder = (data) => {
  return post('/wxmini/pay/order/create', data)
}

const queryOrder = (outTradeNo) => {
  return get('/wxmini/pay/order/query', { outTradeNo })
}

module.exports = {
  getOrderList,
  getOrderDetail,
  createOrder,
  cancelOrder,
  writeOffOrder,
  createPayOrder,
  queryOrder
}
