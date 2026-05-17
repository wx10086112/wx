const { get, put } = require('../utils/request')

const getMerchantWorkbenchOverview = () => get('/wxmini/merchant-mini/workbench/overview')
const getMerchantProfile = () => get('/wxmini/merchant-mini/store/profile')
const updateMerchantProfile = (data) => put('/wxmini/merchant-mini/store/profile', data)

module.exports = {
  getMerchantWorkbenchOverview,
  getMerchantProfile,
  updateMerchantProfile
}
