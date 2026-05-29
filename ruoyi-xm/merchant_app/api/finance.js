const { get } = require('../utils/request')

const getSettlementOverview = () => get('/wxmini/merchant-mini/settlement/overview')

module.exports = {
  getSettlementOverview,
  getFinanceOverview: getSettlementOverview
}
