const authApi = require('./auth')
const merchantApi = require('./merchant')
const orderApi = require('./order')
const goodsApi = require('./goods')
const staffApi = require('./staff')
const verifyApi = require('./verify')
const financeApi = require('./finance')
const marketingApi = require('./marketing')

module.exports = {
  ...authApi,
  ...merchantApi,
  ...orderApi,
  ...goodsApi,
  ...staffApi,
  ...verifyApi,
  ...financeApi,
  ...marketingApi
}
