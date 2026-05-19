const api = require('../../api/index')
const util = require('../../utils/util')

const app = getApp()

Page({
  data: {
    username: '',
    password: ''
  },

  onLoad() {
    if (app.globalData.isLoggedIn) {
      wx.switchTab({
        url: '/pages/workbench/workbench'
      })
    }
  },

  handleUsernameInput(e) {
    this.setData({ username: e.detail.value })
  },

  handlePasswordInput(e) {
    this.setData({ password: e.detail.value })
  },

  goApply() {
    util.navigateTo('/pages/apply/apply')
  },

  submitLogin() {
    const { username, password } = this.data
    if (!username) {
      util.showToast('请输入用户名')
      return
    }
    if (!password) {
      util.showToast('请输入密码')
      return
    }

    wx.showLoading({
      title: '登录中',
      mask: true
    })

    api
      .merchantLogin({ username, password })
      .then((response) => {
        app.setLoginInfo(response.token, response.staffUser)
        const merchantName = response.staffUser.merchantName || ''
        if (merchantName) {
          util.showToast('欢迎回来，' + merchantName, 'success')
        }
      })
      .catch(() => {
        util.showToast('用户名或密码错误')
      })
      .finally(() => {
        wx.hideLoading()
        if (app.globalData.isLoggedIn) {
          wx.switchTab({
            url: '/pages/workbench/workbench'
          })
        }
      })
  }
})
