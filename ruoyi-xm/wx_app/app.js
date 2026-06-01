const util = require('./utils/util')
const merchantUtil = require('./utils/merchant-util')
const merchantMock = require('./data/merchant-mock')

App({
  globalData: {
    userInfo: null,
    token: null,
    isLoggedIn: false,
    staffUser: null,
    merchantToken: null,
    isMerchantLoggedIn: false,
    permissionCodes: [],
    appId: ''
  },

  onLaunch() {
    this.initEnv()
    this.checkLoginStatus()
    this.restoreMerchantLogin()
    merchantUtil.initMerchantMockStorage(merchantMock)
  },

  initEnv() {
    this.baseUrl = 'http://localhost:8080'
    const accountInfo = wx.getAccountInfoSync()
    if (accountInfo && accountInfo.miniProgram) {
      this.globalData.appId = accountInfo.miniProgram.appId || ''
    }
  },

  checkLoginStatus() {
    const token = wx.getStorageSync('token')
    const userInfo = util.normalizeImageFields(wx.getStorageSync('userInfo'))
    util.getStoredOrderList()
    if (token && userInfo) {
      this.globalData.token = token
      this.globalData.userInfo = userInfo
      this.globalData.isLoggedIn = true
      wx.setStorageSync('userInfo', userInfo)
    }
  },

  restoreMerchantLogin() {
    const merchantToken = wx.getStorageSync('merchantToken')
    const staffUser = wx.getStorageSync('merchantStaffUser')
    if (merchantToken && staffUser) {
      this.globalData.merchantToken = merchantToken
      this.globalData.staffUser = staffUser
      this.globalData.isMerchantLoggedIn = true
      this.globalData.permissionCodes = staffUser.permissions || []
    }
  },

  setLoginInfo(token, userInfo) {
    const normalizedUserInfo = util.normalizeImageFields(userInfo)
    this.globalData.token = token
    this.globalData.userInfo = normalizedUserInfo
    this.globalData.isLoggedIn = true
    wx.setStorageSync('token', token)
    wx.setStorageSync('userInfo', normalizedUserInfo)
  },

  clearLoginInfo() {
    this.globalData.token = null
    this.globalData.userInfo = null
    this.globalData.isLoggedIn = false
    wx.removeStorageSync('token')
    wx.removeStorageSync('userInfo')
  },

  setMerchantLoginInfo(token, staffUser) {
    this.globalData.merchantToken = token
    this.globalData.staffUser = staffUser
    this.globalData.isMerchantLoggedIn = true
    this.globalData.permissionCodes = staffUser.permissions || []
    wx.setStorageSync('merchantToken', token)
    wx.setStorageSync('merchantStaffUser', staffUser)
  },

  clearMerchantLoginInfo() {
    this.globalData.merchantToken = null
    this.globalData.staffUser = null
    this.globalData.isMerchantLoggedIn = false
    this.globalData.permissionCodes = []
    wx.removeStorageSync('merchantToken')
    wx.removeStorageSync('merchantStaffUser')
  },

  needLogin() {
    if (!this.globalData.isLoggedIn) {
      wx.showModal({
        title: '提示',
        content: '请先登录',
        success: (res) => {
          if (res.confirm) {
            wx.switchTab({
              url: '/pages/mine/mine'
            })
          }
        }
      })
      return false
    }
    return true
  },

  needMerchantLogin() {
    if (this.globalData.isMerchantLoggedIn) return true
    wx.redirectTo({
      url: '/pages/merchant/login/login'
    })
    return false
  },

  hasPermission(code) {
    return (this.globalData.permissionCodes || []).includes(code)
  },

  hasAnyPermission(codeList = []) {
    if (!codeList.length) return true
    return codeList.some((code) => this.hasPermission(code))
  },

  needPermission(codeList = [], message = '当前账号暂无该操作权限') {
    if (this.hasAnyPermission(codeList)) return true
    merchantUtil.showToast(message)
    return false
  }
})
