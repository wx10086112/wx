const { get, put } = require('../utils/request')

const getStaffList = () => get('/wxmini/merchant-mini/staff/list')
const updateStaffPermission = (data) => put('/wxmini/merchant-mini/staff/permission', data)

module.exports = {
  getStaffList,
  updateStaffPermission
}
