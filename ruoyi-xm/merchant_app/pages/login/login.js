const mock = require('../../data/mock')
const api = require('../../api/index')
const util = require('../../utils/util')

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
    if (app.globalData.isLoggedIn) {
      wx.switchTab({
        url: '/pages/workbench/workbench'
      })
    }
  },

  handleRoleTap(e) {
    this.setData({
      selectedRoleKey: e.currentTarget.dataset.role
    })
  },

  goApply() {
    util.navigateTo('/pages/apply/apply')
  },

  submitLogin() {
    wx.showLoading({
      title: '登录中',
      mask: true
    })

    api
      .merchantLogin({
        roleKey: this.data.selectedRoleKey
      })
      .then((response) => {
        app.setLoginInfo(response.token, response.staffUser)
      })
      .catch(() => {
        const staffUser = mock.buildStaffUser(this.data.selectedRoleKey)
        app.setLoginInfo(`merchant_token_${Date.now()}`, staffUser)
        util.showToast('后端未联通，已切换本地演示模式')
      })
      .finally(() => {
        wx.hideLoading()
        wx.switchTab({
          url: '/pages/workbench/workbench'
        })
      })
  }
})
