const util = require('./utils/util')

App({
  globalData: {
    userInfo: null,
    token: null,
    isLoggedIn: false
  },

  onLaunch() {
    this.checkLoginStatus()
    this.initEnv()
  },

  initEnv() {
    this.baseUrl = 'http://localhost:8080'
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
  }
})
