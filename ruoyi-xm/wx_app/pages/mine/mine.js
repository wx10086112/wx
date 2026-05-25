const app = getApp()
const util = require('../../utils/util')
const templateService = require('../../services/template')
const userApi = require('../../api/user')

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
    benefitTagList: [],
    loginStep: 'idle'
  },

  onLoad() {
    this.initTemplateConfig()
    this.checkLoginStatus()
  },

  onShow() {
    this.checkLoginStatus()
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
        return true
      }),
      orderEntryList: (profileConfig.orderEntries || []).map((item) => ({ ...item, badge: 0 })),
      assetCardList: (profileConfig.assetEntries || []).map((item) => ({ ...item, count: 0 })),
      benefitTagList: (profileConfig.benefitTips || []).slice(0, 4)
    })
  },

  checkLoginStatus() {
    this.setData({
      isLoggedIn: app.globalData.isLoggedIn,
      userInfo: app.globalData.userInfo || {}
    })
  },

  handleLogin() {
    this.setData({ loginStep: 'loading' })
    util.showLoading('登录中...')

    // 测试登录：直接调用测试接口，不走微信验证
    userApi
      .testLogin(app.appId || 'wx6c708117ea8eaab4')
      .then((res) => {
        const info = res || {}
        const userInfo = {
          userId: info.userId || '',
          openId: info.openId || '',
          nickName: info.userName || '微信用户',
          avatarUrl: info.avatarUrl || '/assets/images/avatar.png',
          phone: info.phone || ''
        }
        const token = info.apiToken || ''

        if (!token) {
          util.hideLoading()
          util.showToast('登录失败，请重试')
          this.setData({ loginStep: 'idle' })
          return
        }

        app.setLoginInfo(token, userInfo)

        this.setData({
          isLoggedIn: true,
          userInfo,
          loginStep: 'idle'
        })
        util.hideLoading()
        util.showToast('登录成功', 'success')
      })
      .catch(() => {
        util.hideLoading()
        util.showToast('登录失败，请重试')
        this.setData({ loginStep: 'idle' })
      })
  },

  handlePhoneAuth(e) {
    if (e.detail.errMsg !== 'getPhoneNumber:ok') {
      util.showToast('需要授权手机号才能使用完整功能')
      return
    }

    const phoneCode = e.detail.code
    if (!phoneCode) {
      util.showToast('获取手机号失败，请重试')
      return
    }

    util.showLoading('绑定中...')
    userApi
      .bindPhoneByCode(phoneCode)
      .then((res) => {
        const info = res || {}
        if (info.phone) {
          const userInfo = { ...this.data.userInfo, phone: info.phone }
          app.setLoginInfo(app.globalData.token, userInfo)
          this.setData({ userInfo, loginStep: 'idle' })
        }
        util.hideLoading()
        util.showToast('登录成功', 'success')
      })
      .catch(() => {
        util.hideLoading()
        this.setData({ loginStep: 'idle' })
        util.showToast('手机号绑定失败，可稍后在个人中心补充')
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
    util.navigateTo('/pages/order/order')
  },

  goOrderStatus(e) {
    util.setPendingOrderFilter(e.currentTarget.dataset.status)
    util.navigateTo('/pages/order/order')
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
  },

  goProfileEdit() {
    util.navigateTo('/pages/profile-edit/profile-edit')
  }
})
