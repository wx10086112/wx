const { get, put, post } = require('../utils/request')

const getStaffList = () => get('/wxmini/merchant-mini/staff/list')
const updateStaffPermission = (data) => put('/wxmini/merchant-mini/staff/permission', data)
const addMerchantStaff = (data) => post('/wxmini/merchant-mini/staff/add', data)
const updateMerchantStaff = (data) => put('/wxmini/merchant-mini/staff/update', data)

module.exports = {
  getStaffList,
  updateStaffPermission,
  addMerchantStaff,
  updateMerchantStaff
}
