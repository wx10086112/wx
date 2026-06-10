const app = getApp()
const util = require('../../utils/util')
const agreement = require('../../utils/agreement')
const userApi = require('../../api/user')
const templateService = require('../../services/template')
const merchantEntry = require('../../utils/merchant-entry')

const DEFAULT_BRAND_NAME = '鼎立老碗葫芦头'
const DEFAULT_BRAND_LOGO = '/assets/images/merchant-logo-dingli.jpg'
const DEFAULT_BRAND_SUBTITLE = '生活有点苦，今天团点甜'

const isPlaceholderBrandName = (value = '') => {
  const text = String(value || '').trim()
  return !text ||
    text === '商家名称' ||
    /mall\.privacy\.operatorName|后台参数|配置运营主体/.test(text)
}

const parseMerchantIdFromOptions = (options = {}) => {
  const directMerchantId = Number(options.merchantId || 0)
  if (directMerchantId) return directMerchantId

  const scene = decodeURIComponent(options.scene || '')
  const match = scene.match(/(?:^|&)merchantId=(\d+)(?:&|$)/)
  return match ? Number(match[1]) : 0
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
    brandLogo: DEFAULT_BRAND_LOGO,
    brandSubtitle: DEFAULT_BRAND_SUBTITLE,
    agreementAccepted: false,
    showAgreementModal: false,
    submitting: false
  },

  onLoad(options = {}) {
    if (this.redirectMerchantLoginIfNeeded(options)) {
      return
    }
    this.initBrand()
    this.syncAgreementState()
    if (app.globalData.isLoggedIn) {
      this.goMine()
    }
  },

  redirectMerchantLoginIfNeeded(options = {}) {
    const merchantId = parseMerchantIdFromOptions(options)
    if (!merchantId) return false

    if (app.setMerchantEntry) {
      app.setMerchantEntry({ merchantId })
    }
    wx.redirectTo({
      url: `/pages/merchant/login/login?merchantId=${merchantId}`
    })
    return true
  },

  onShow() {
    this.syncAgreementState()
  },

  initBrand() {
    const brandInfo = templateService.getTemplateSection('brandInfo') || {}
    const title = isPlaceholderBrandName(brandInfo.name) ? DEFAULT_BRAND_NAME : brandInfo.name
    this.setData({
      brandTitle: title,
      brandInitial: title.slice(0, 1).toUpperCase(),
      brandLogo: brandInfo.logo || brandInfo.avatar || DEFAULT_BRAND_LOGO,
      brandSubtitle: brandInfo.slogan || DEFAULT_BRAND_SUBTITLE
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

  openAgreementDetail(e) {
    const type = (e && e.currentTarget && e.currentTarget.dataset.type) || 'service'
    this.setData({ showAgreementModal: false }, () => {
      const agreementDetail = this.selectComponent('#agreementDetail')
      if (agreementDetail && agreementDetail.openAgreementPanel) {
        agreementDetail.openAgreementPanel(type)
      }
    })
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
