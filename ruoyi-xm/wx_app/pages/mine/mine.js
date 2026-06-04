const app = getApp()
const util = require('../../utils/util')

const MENU_LIST = [
  { label: '我的订单', type: 'order', icon: 'order' },
  { label: '个人资料', type: 'profile', icon: 'profile' },
  { label: '在线客服', type: 'contact', icon: 'service' },
  { label: '分享小程序', type: 'share', icon: 'share' }
]

Page({
  data: {
    isLoggedIn: false,
    userInfo: {},
    menuList: MENU_LIST
  },

  onLoad() {
    this.checkLoginStatus()
  },

  onShow() {
    this.checkLoginStatus()
    if (!this.data.isLoggedIn) {
      this.goLogin()
    }
  },

  checkLoginStatus() {
    this.setData({
      isLoggedIn: app.globalData.isLoggedIn,
      userInfo: app.globalData.userInfo || {}
    })
  },

  goLogin() {
    wx.navigateTo({
      url: '/pages/login/login'
    })
  },

  goSettings() {
    util.navigateTo('/pages/settings/settings')
  },

  goProfileEdit() {
    util.navigateTo('/pages/profile-edit/profile-edit')
  },

  goOrder() {
    wx.switchTab({
      url: '/pages/order/order'
    })
  },

  goContact() {
    util.navigateTo('/pages/contact/contact')
  },

  goMenu(e) {
    const type = e.currentTarget.dataset.type
    if (type === 'order') {
      this.goOrder()
      return
    }
    if (type === 'profile') {
      this.goProfileEdit()
      return
    }
    if (type === 'contact') {
      this.goContact()
    }
  },

  onShareAppMessage() {
    return {
      title: '本地生活服务',
      path: '/pages/home/home'
    }
  }
})
