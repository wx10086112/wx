const app = getApp()
const util = require('../../utils/util')
const agreement = require('../../utils/agreement')
const userApi = require('../../api/user')
const templateService = require('../../services/template')
const merchantEntry = require('../../utils/merchant-entry')

const isLocalTestLogin = () => {
  const baseUrl = String(app.baseUrl || wx.getStorageSync('baseUrl') || '')
  return /^https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?$/i.test(baseUrl)
}

const normalizeLoginUser = (info = {}) => {
  return {
    userId: info.userId || '',
    openId: info.openId || '',
    nickName: info.userName || '微信用户',
    avatarUrl: info.avatarUrl || '/assets/images/avatar.svg',
    phone: info.phone || '',
    merchantId: info.merchantId || null,
    merchantName: info.merchantName || '',
    appId: info.appId || app.globalData.appId || ''
  }
}

Page({
  data: {
    brandTitle: '欢迎登录',
    brandInitial: 'W',
    agreementAccepted: false,
    showAgreementModal: false,
    submitting: false
  },

  onLoad() {
    this.initBrand()
    this.syncAgreementState()
    if (app.globalData.isLoggedIn) {
      this.goMine()
    }
  },

  onShow() {
    this.syncAgreementState()
  },

  initBrand() {
    const brandInfo = templateService.getTemplateSection('brandInfo') || {}
    const title = brandInfo.name && brandInfo.name !== '商家名称' ? brandInfo.name : '本地生活服务'
    this.setData({
      brandTitle: title,
      brandInitial: title.slice(0, 1).toUpperCase()
    })
  },

  syncAgreementState() {
    this.setData({
      agreementAccepted: agreement.isAgreementAccepted()
    })
  },

  toggleAgreement() {
    const nextChecked = !this.data.agreementAccepted
    if (nextChecked) {
      agreement.acceptAgreement()
    } else {
      agreement.rejectAgreement()
    }
    this.setData({ agreementAccepted: nextChecked })
  },

  openAgreementModal() {
    this.setData({ showAgreementModal: true })
  },

  closeAgreementModal() {
    this.setData({ showAgreementModal: false })
  },

  openAgreementDetail() {
    util.navigateTo('/pages/settings/settings')
  },

  handleWechatLogin() {
    if (this.data.submitting) return
    if (!this.data.agreementAccepted) {
      this.openAgreementModal()
      return
    }
    this.loginByWechat()
  },

  confirmAgreementAndLogin() {
    agreement.acceptAgreement()
    this.setData({
      agreementAccepted: true,
      showAgreementModal: false
    })
    this.loginByWechat()
  },

  loginByWechat() {
    this.setData({ submitting: true })
    util.showLoading('登录中...')

    if (isLocalTestLogin()) {
      this.loginWithTestAccount()
      return
    }

    wx.login({
      success: (loginRes) => {
        if (!loginRes.code) {
          this.handleLoginFail('登录失败，请重试')
          return
        }

        userApi
          .login(app.globalData.appId, loginRes.code)
          .then((res) => this.applyLoginResult(res))
          .catch(() => this.handleLoginFail('登录失败，请重试'))
      },
      fail: () => this.handleLoginFail('登录失败，请重试')
    })
  },

  loginWithTestAccount() {
    userApi
      .testLogin(app.globalData.appId)
      .then((res) => this.applyLoginResult(res))
      .catch(() => this.handleLoginFail('测试登录失败，请检查后端配置'))
  },

  applyLoginResult(res) {
    const info = res.data || {}
    const token = info.apiToken || ''

    if (!token) {
      this.handleLoginFail('登录失败，请重试')
      return
    }

    app.setLoginInfo(token, normalizeLoginUser(info))
    util.hideLoading()
    this.setData({ submitting: false })
    util.showToast('登录成功', 'success')
    setTimeout(() => this.goMine(), 350)
  },

  handleLoginFail(message) {
    util.hideLoading()
    this.setData({ submitting: false })
    util.showToast(message)
  },

  goMine() {
    wx.switchTab({ url: '/pages/mine/mine' })
  },

  goHome() {
    wx.switchTab({ url: '/pages/home/home' })
  },

  goMerchantEntry() {
    merchantEntry.openMerchantPortal()
  },

  preventMove() {}
})
