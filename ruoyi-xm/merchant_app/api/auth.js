const { post } = require('../utils/request')

const merchantLogin = (data) => post('/wxmini/merchant-mini/auth/login', data)

module.exports = {
  merchantLogin
}
