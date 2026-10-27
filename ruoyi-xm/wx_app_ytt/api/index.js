const userApi = require('./user')
const merchantApi = require('./merchant')
const productApi = require('./product')
const orderApi = require('./order')
const templateApi = require('./template')

module.exports = {
  ...userApi,
  ...merchantApi,
  ...productApi,
  ...orderApi,
  ...templateApi
}
