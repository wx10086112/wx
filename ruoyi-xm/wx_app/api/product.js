const { get, post } = require('./request')

const getGrouponList = (data = {}) => {
  return get('/wxmini/groupon/list', data)
}

const getGrouponVersion = (data = {}) => {
  return get('/wxmini/groupon/version', data)
}

const getGrouponDetail = (id) => {
  return get(`/wxmini/groupon/detail/${id}`)
}

const getProductList = (data = {}) => {
  return get('/wxmini/groupon/list', data)
}

const getProductDetail = (id) => {
  return get(`/wxmini/groupon/detail/${id}`)
}

module.exports = {
  getGrouponList,
  getGrouponVersion,
  getGrouponDetail,
  getProductList,
  getProductDetail
}
