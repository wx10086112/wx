const { get, post } = require('../utils/request')

const getFinanceOverview = () => get('/wxmini/merchant-mini/finance/overview')
const applyFinanceWithdraw = (data) => post('/wxmini/merchant-mini/finance/withdraw', data)

module.exports = {
  getFinanceOverview,
  applyFinanceWithdraw
}
