const { get, post } = require('../utils/request')

const getSettlementOverview = () => get('/wxmini/merchant-mini/settlement/overview')
const getFinanceOverview = getSettlementOverview
const applyFinanceWithdraw = (data) => post('/wxmini/merchant-mini/finance/withdraw', data)

module.exports = {
  getSettlementOverview,
  getFinanceOverview,
  applyFinanceWithdraw
}
