const app = getApp()
const mock = require('../../data/mock')
const util = require('../../utils/util')
const templateService = require('../../services/template')

Page({
  data: {
    isLoggedIn: false,
    userInfo: {},
    profileConfig: {},
    orderEntryList: [],
    assetCardList: [],
    serviceMenuList: [],
    featureToggle: {},
    couponCount: 0,
    favoriteCount: 0,
    benefitTagList: []
  },

  onLoad() {
    this.initTemplateConfig()
    this.checkLoginStatus()
    this.loadAssets()
  },

  onShow() {
    this.checkLoginStatus()
    this.loadAssets()
  },

  initTemplateConfig() {
    const profileConfig = templateService.getTemplateSection('profile')
    const featureToggle = templateService.getTemplateSection('featureToggle')
    this.setData({
      profileConfig,
      featureToggle,
      serviceMenuList: (profileConfig.serviceMenus || []).filter((item) => {
        if (item.url === '/pages/coupon/coupon') return featureToggle.enableCoupon
        if (item.url === '/pages/favorite/favorite') return featureToggle.enableFavorite
        if (item.url === '/pages/review/review-list') return featureToggle.enableReview
        return true
      })
    })
  },

  checkLoginStatus() {
    this.setData({
      isLoggedIn: app.globalData.isLoggedIn,
      userInfo: app.globalData.userInfo || {}
    })
  },

  buildOrderCountMap(orderList = []) {
    return {
      PENDING_PAY: orderList.filter((item) => item.status === 'PENDING_PAY').length,
      UNUSED: orderList.filter((item) => item.status === 'PAID_UNUSED').length,
      AFTER_SALE: orderList.filter((item) => ['REFUNDING', 'REFUNDED'].includes(item.status)).length
    }
  },

  buildOrderEntryList(profileConfig = {}, orderCountMap = {}) {
    return (profileConfig.orderEntries || []).map((item) => ({
      ...item,
      badge: item.status ? orderCountMap[item.status] || 0 : 0
    }))
  },

  buildBenefitTags(profileConfig = {}, counters = {}, orderCountMap = {}) {
    const dynamicTags = []

    if (counters.couponCount) {
      dynamicTags.push(`${counters.couponCount} 张券待使用`)
    }
    if (orderCountMap.UNUSED) {
      dynamicTags.push(`${orderCountMap.UNUSED} 个待核销订单`)
    }
    if (counters.favoriteCount) {
      dynamicTags.push(`${counters.favoriteCount} 个收藏内容`)
    }
    return dynamicTags.concat(profileConfig.benefitTips || []).slice(0, 4)
  },

  loadAssets() {
    const profileConfig =
      this.data.profileConfig && this.data.profileConfig.assetEntries
        ? this.data.profileConfig
        : templateService.getTemplateSection('profile')
    const storedOrderList = util.getStoredOrderList(mock.orderList)
    const orderCountMap = this.buildOrderCountMap(storedOrderList)
    const counters = {
      couponCount: mock.couponList.filter((item) => item.status === 'AVAILABLE').length,
      favoriteCount: mock.favoriteList.length
    }
    const assetCardList = (profileConfig.assetEntries || [])
      .filter((item) => {
        if (item.url === '/pages/coupon/coupon') return this.data.featureToggle.enableCoupon
        if (item.url === '/pages/favorite/favorite') return this.data.featureToggle.enableFavorite
        return true
      })
      .map((item) => ({
        ...item,
        count: counters[item.countField] || 0
      }))

    this.setData({
      ...counters,
      assetCardList,
      orderEntryList: this.buildOrderEntryList(profileConfig, orderCountMap),
      benefitTagList: this.buildBenefitTags(profileConfig, counters, orderCountMap)
    })
  },

  handleLogin() {
    util.showLoading('登录中...')
    wx.login({
      success: (res) => {
        if (!res.code) {
          util.hideLoading()
          util.showToast('登录失败')
          return
        }
        setTimeout(() => {
          const userInfo = {
            ...mock.userInfo,
            nickName: '微信用户'
          }
          app.setLoginInfo('mock_token_' + Date.now(), userInfo)
          this.setData({
            isLoggedIn: true,
            userInfo
          })
          util.hideLoading()
          util.showToast('登录成功', 'success')
        }, 500)
      },
      fail: () => {
        util.hideLoading()
        util.showToast('登录失败')
      }
    })
  },

  handleLogout() {
    util.showModal('退出登录', '确定退出当前账号吗？').then((confirm) => {
      if (!confirm) return
      app.clearLoginInfo()
      this.setData({
        isLoggedIn: false,
        userInfo: {}
      })
      util.showToast('已退出登录', 'success')
    })
  },

  goOrder() {
    util.switchTab('/pages/order/order')
  },

  goOrderStatus(e) {
    util.setPendingOrderFilter(e.currentTarget.dataset.status)
    util.switchTab('/pages/order/order')
  },

  handleOrderEntry(e) {
    const { status, url } = e.currentTarget.dataset
    if (status) {
      this.goOrderStatus(e)
      return
    }
    if (url) {
      util.navigateTo(url)
    }
  },

  goPage(e) {
    util.navigateTo(e.currentTarget.dataset.url)
  }
})
