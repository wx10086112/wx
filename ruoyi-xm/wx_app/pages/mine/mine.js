const app = getApp()
const util = require('../../utils/util')
const templateService = require('../../services/template')
const merchantApi = require('../../api/merchant')
const merchantEntry = require('../../utils/merchant-entry')

const MENU_LIST = [
  { label: '个人资料', type: 'profile', icon: 'profile' },
  { label: '我的预点单', type: 'booking', icon: 'booking' },
  { label: '在线客服', type: 'contact', icon: 'service' },
  { label: '设置', type: 'settings', icon: 'settings' },
  { label: '商家入口', type: 'merchant', icon: 'merchant' }
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
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 3 })
    }
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

  goBooking() {
    util.navigateTo('/pages/my-booking/my-booking')
  },

  goContact() {
    util.showLoading('获取电话中...')
    const brandInfo = templateService.getTemplateSection('brandInfo') || {}
    const fallbackPhone = brandInfo.servicePhone || ''

    merchantApi.getMerchantList()
      .then((res) => {
        const merchant = (res.data || res || [])[0] || {}
        const phone = merchant.phone || fallbackPhone
        util.hideLoading()
        if (!phone) {
          util.showToast('暂无客服电话')
          return
        }
        wx.makePhoneCall({
          phoneNumber: String(phone),
          fail: () => {}
        })
      })
      .catch(() => {
        util.hideLoading()
        if (!fallbackPhone) {
          util.showToast('暂无客服电话')
          return
        }
        wx.makePhoneCall({
          phoneNumber: String(fallbackPhone),
          fail: () => {}
        })
      })
  },

  goMerchantEntry() {
    merchantEntry.openMerchantPortal()
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
    if (type === 'booking') {
      this.goBooking()
      return
    }
    if (type === 'contact') {
      this.goContact()
      return
    }
    if (type === 'settings') {
      this.goSettings()
      return
    }
    if (type === 'merchant') {
      this.goMerchantEntry()
    }
  },

  onShareAppMessage() {
    return {
      title: '本地生活服务',
      path: '/pages/home/home'
    }
  }
})
