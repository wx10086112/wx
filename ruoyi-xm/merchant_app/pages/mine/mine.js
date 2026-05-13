const util = require('../../utils/util')

const app = getApp()

Page({
  data: {
    staffUser: {},
    permissionList: [],
    storeInfo: {}
  },

  onShow() {
    if (!app.needLogin()) return
    this.setData({
      staffUser: app.globalData.staffUser || {},
      permissionList: app.globalData.permissionCodes || [],
      storeInfo: util.getStoreInfo()
    })
  },

  goStore() {
    util.navigateTo('/pages/store/store')
  },

  goStaff() {
    util.navigateTo('/pages/staff/staff')
  },

  goVerifyRecords() {
    util.navigateTo('/pages/verify-records/verify-records')
  },

  goFinance() {
    util.navigateTo('/pages/finance/finance')
  },

  logout() {
    app.clearLoginInfo()
    wx.redirectTo({
      url: '/pages/login/login'
    })
  }
})
