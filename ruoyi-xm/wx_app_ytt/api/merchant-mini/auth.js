const { post } = require('../../utils/merchant-request')

const merchantLogin = (data) => post('/wxmini/merchant-mini/auth/login', data)

module.exports = {
  merchantLogin
}
