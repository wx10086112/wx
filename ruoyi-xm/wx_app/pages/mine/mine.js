const app = getApp()
const mock = require('../../data/mock')
const util = require('../../utils/util')
const templateService = require('../../services/template')
const userApi = require('../../api/user')
const orderApi = require('../../api/order')

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
        return true
      })
    }, () => {
      this.loadAssets(profileConfig, featureToggle)
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

  loadAssets(profileConfigArg, featureToggleArg) {
<<<<<<< HEAD
=======
    if (!app.globalData.isLoggedIn) return

>>>>>>> 苏
    const profileConfig =
      profileConfigArg || (this.data.profileConfig && this.data.profileConfig.assetEntries
        ? this.data.profileConfig
        : templateService.getTemplateSection('profile')
      )
    const featureToggle =
      featureToggleArg || (this.data.featureToggle && Object.keys(this.data.featureToggle).length
        ? this.data.featureToggle
        : templateService.getTemplateSection('featureToggle')
      )
<<<<<<< HEAD
    const storedOrderList = util.getStoredOrderList(mock.orderList)
    const orderCountMap = this.buildOrderCountMap(storedOrderList)
    const counters = {
      couponCount: mock.couponList.filter((item) => item.status === 'AVAILABLE').length,
      favoriteCount: mock.favoriteList.length
    }
=======

    // 从真实API获取订单数统计
    orderApi.getOrderList()
      .then((res) => {
        const orderList = res.data || res || []
        const orderCountMap = this.buildOrderCountMap(orderList)
        const counters = {
          couponCount: 0,
          favoriteCount: 0
        }

        this.renderAssets(profileConfig, featureToggle, counters, orderCountMap)
      })
      .catch(() => {
        const counters = { couponCount: 0, favoriteCount: 0 }
        const orderCountMap = this.buildOrderCountMap([])
        this.renderAssets(profileConfig, featureToggle, counters, orderCountMap)
      })
  },

  renderAssets(profileConfig, featureToggle, counters, orderCountMap) {
>>>>>>> 苏
    const assetCardList = (profileConfig.assetEntries || [])
      .filter((item) => {
        if (item.url === '/pages/coupon/coupon') return featureToggle.enableCoupon
        if (item.url === '/pages/favorite/favorite') return featureToggle.enableFavorite
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
    this.setData({ loginStep: 'loading' })
    util.showLoading('登录中...')

    wx.login({
      success: (loginRes) => {
        if (!loginRes.code) {
          util.hideLoading()
          util.showToast('登录失败，请重试')
          this.setData({ loginStep: 'idle' })
          return
        }

        userApi
<<<<<<< HEAD
          .login(app.appId || 'wx6c708117ea8eaab4', loginRes.code)
=======
          .login(app.globalData.appId, loginRes.code)
>>>>>>> 苏
          .then((res) => {
            const info = res.data || {}
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

            if (!userInfo.phone) {
              util.hideLoading()
              this.setData({
                isLoggedIn: true,
                userInfo,
                loginStep: 'phone'
              })
              return
            }

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
      fail: () => {
        util.hideLoading()
        util.showToast('登录失败，请重试')
        this.setData({ loginStep: 'idle' })
      }
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
        const info = res.data || {}
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
