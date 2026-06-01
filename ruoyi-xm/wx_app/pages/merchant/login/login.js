const api = require('../../../api/merchant-mini/index')
const util = require('../../../utils/merchant-util')

const app = getApp()

Page({
  data: {
    roleCardList: [
      {
        roleKey: 'manager',
        title: '店长登录',
        desc: '拥有订单、核销、商品、门店和员工权限'
      },
      {
        roleKey: 'clerk',
        title: '店员登录',
        desc: '聚焦订单处理与到店核销日常操作'
      }
    ],
    selectedRoleKey: 'manager'
  },

  onLoad() {
    if (app.globalData.isMerchantLoggedIn) {
      wx.redirectTo({
        url: '/pages/merchant/workbench/workbench'
      })
    }
  },

  onUsernameInput(e) {
    this.setData({ username: e.detail.value })
  },

  onPasswordInput(e) {
    this.setData({ password: e.detail.value })
  },

  goApply() {
    util.navigateTo('/pages/merchant/apply/apply')
  },

  submitLogin() {
    var username = (this.data.username || '').trim()
    var password = (this.data.password || '').trim()

    if (!username) {
      util.showToast('请输入账号')
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
      .merchantLogin({
        username: username,
        password: password
      })
      .then((response) => {
        app.setMerchantLoginInfo(response.token, response.staffUser)
        wx.redirectTo({
          url: '/pages/merchant/workbench/workbench'
        })
      })
      .catch((err) => {
        util.showToast(err.message || '登录失败，请检查账号密码')
      })
      .finally(() => {
        wx.hideLoading()
      })
  }
})
