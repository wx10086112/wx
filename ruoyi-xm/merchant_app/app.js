const util = require('./utils/util')

App({
  globalData: {
    staffUser: null,
    token: null,
    isLoggedIn: false,
    permissionCodes: []
  },

  onLaunch() {
    this.initEnv()
    this.restoreLogin()
  },

  initEnv() {
    this.baseUrl = 'http://localhost:8080'
    this.appId = wx.getAccountInfoSync().miniProgram.appId || ''
  },

  restoreLogin() {
    const token = wx.getStorageSync('merchantToken')
    const staffUser = wx.getStorageSync('merchantStaffUser')
    if (token && staffUser) {
      this.globalData.token = token
      this.globalData.staffUser = staffUser
      this.globalData.isLoggedIn = true
      this.globalData.permissionCodes = staffUser.permissions || []
    }
  },

  setLoginInfo(token, staffUser) {
    this.globalData.token = token
    this.globalData.staffUser = staffUser
    this.globalData.isLoggedIn = true
    this.globalData.permissionCodes = staffUser.permissions || []
    wx.setStorageSync('merchantToken', token)
    wx.setStorageSync('merchantStaffUser', staffUser)
  },

  clearLoginInfo() {
    this.globalData.token = null
    this.globalData.staffUser = null
    this.globalData.isLoggedIn = false
    this.globalData.permissionCodes = []
    wx.removeStorageSync('merchantToken')
    wx.removeStorageSync('merchantStaffUser')
  },

  hasPermission(code) {
    return (this.globalData.permissionCodes || []).includes(code)
  },

  hasAnyPermission(codeList = []) {
    if (!codeList.length) return true
    return codeList.some((code) => this.hasPermission(code))
  },

  needLogin() {
    if (this.globalData.isLoggedIn) return true
    wx.redirectTo({
      url: '/pages/login/login'
    })
    return false
  },

  needPermission(codeList = [], message = '当前账号暂无该操作权限') {
    if (this.hasAnyPermission(codeList)) {
      return true
    }
    util.showToast(message)
    return false
  }
})
