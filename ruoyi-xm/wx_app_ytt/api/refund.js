const { get, post } = require('./request')

const applyRefund = (data = {}) => {
  return post('/wxmini/refund/apply', data)
}

const getRefundList = () => {
  return get('/wxmini/refund/list')
}

module.exports = {
  applyRefund,
  getRefundList
}
