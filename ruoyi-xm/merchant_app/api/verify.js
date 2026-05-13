const { get } = require('../utils/request')

const getVerifyRecordList = (data = {}) => get('/wxmini/merchant-mini/verify/record/list', data)

module.exports = {
  getVerifyRecordList
}
