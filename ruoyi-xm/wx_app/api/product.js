const { get, post } = require('./request')

const getGrouponList = (data = {}) => {
  return get('/wxmini/groupon/list', data)
}

const getGrouponDetail = (id) => {
  return get(`/wxmini/groupon/detail/${id}`)
}

const getProductList = (data = {}) => {
  return get('/wxmini/product/list', data)
}

const getProductDetail = (id) => {
  return get(`/wxmini/product/detail/${id}`)
}

module.exports = {
  getGrouponList,
  getGrouponDetail,
  getProductList,
  getProductDetail
}
