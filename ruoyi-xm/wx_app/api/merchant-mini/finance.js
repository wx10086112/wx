const { get } = require('../../utils/merchant-request')

const getSettlementOverview = () => get('/wxmini/merchant-mini/settlement/overview')

module.exports = {
  getSettlementOverview,
  getFinanceOverview: getSettlementOverview
}
