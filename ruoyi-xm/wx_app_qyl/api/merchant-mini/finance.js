const { get } = require('../../utils/merchant-request')

const getSettlementOverview = () => get('/wxmini/merchant-mini/settlement/overview')
const getDailyFlow = (range = 'today', date = '') => {
  const params = { range }
  if (date) params.date = date
  return get('/wxmini/merchant-mini/finance/daily-flow', params)
}

module.exports = {
  getSettlementOverview,
  getFinanceOverview: getSettlementOverview,
  getDailyFlow
}
