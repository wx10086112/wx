const app = getApp()
const util = require('../../utils/util')
const agreement = require('../../utils/agreement')
const userApi = require('../../api/user')
const templateService = require('../../services/template')
const merchantEntry = require('../../utils/merchant-entry')

const DEFAULT_BRAND_NAME = '湘缘食尚餐厅(梨园路店)'
const DEFAULT_BRAND_LOGO = '/assets/images/merchant-logo-xiangyuan.png'
const DEFAULT_BRAND_SUBTITLE = '生活有点苦，今天团点甜'
const DEFAULT_USER_NAME = '微信用户'
const DEFAULT_AVATAR_URL = '/assets/images/avatar.svg'

const normalizeReturnUrl = (value = '') => {
  if (!value) return ''
  const decoded = decodeURIComponent(value)
  if (!/^\/pages\//.test(decoded) || /:\/\//.test(decoded)) {
    return ''
  }
  return decoded
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
    nickName: info.userName || DEFAULT_USER_NAME,
    avatarUrl: info.avatarUrl || DEFAULT_AVATAR_URL,
    phone: info.phone || '',
    merchantId: info.merchantId || null,
    merchantName: info.merchantName || '',
    appId: info.appId || app.globalData.appId || ''
  }
}

const isProfileComplete = (userInfo = {}) => {
  const nickName = String(userInfo.nickName || '').trim()
  const avatarUrl = String(userInfo.avatarUrl || '').trim()
  const phone = String(userInfo.phone || '').trim()
  return /^1\d{10}$/.test(phone)
    && nickName
    && nickName !== DEFAULT_USER_NAME
    && avatarUrl
    && avatarUrl !== DEFAULT_AVATAR_URL
}

const getLoginErrorMessage = (err) => {
  return (err && err.message) || '登录失败，请重试'
}

Page({
  data: {
    brandTitle: DEFAULT_BRAND_NAME,
    brandInitial: DEFAULT_BRAND_NAME.slice(0, 1),
    brandLogo: DEFAULT_BRAND_LOGO,
    brandSubtitle: DEFAULT_BRAND_SUBTITLE,
    agreementAccepted: false,
    showAgreementModal: false,
    returnUrl: '',
    submitting: false
  },

  onLoad(options = {}) {
    if (this.redirectMerchantLoginIfNeeded(options)) {
      return
    }
    this.initBrand()
    this.syncAgreementState()
    this.setData({
      returnUrl: normalizeReturnUrl(options.returnUrl)
    })
    if (app.globalData.isLoggedIn) {
      this.goNextAfterLogin(app.globalData.userInfo)
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
    this.applyBrandInfo(templateService.getTemplateSection('brandInfo') || {})
    templateService.fetchTemplateConfig({
      useRemote: true,
      force: true
    }).then((config = {}) => {
      this.applyBrandInfo(config.brandInfo || {})
    }).catch(() => {})
  },

  applyBrandInfo(brandInfo = {}) {
    const title = brandInfo.name || DEFAULT_BRAND_NAME
    this.setData({
      brandTitle: title,
      brandInitial: title.slice(0, 1).toUpperCase(),
      brandLogo: brandInfo.logo || DEFAULT_BRAND_LOGO,
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
          .catch((err) => this.handleLoginFail(getLoginErrorMessage(err)))
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
    setTimeout(() => this.goNextAfterLogin(app.globalData.userInfo), 350)
  },

  handleLoginFail(message) {
    util.hideLoading()
    this.setData({ submitting: false })
    util.showToast(message)
  },

  goMine() {
    wx.switchTab({ url: '/pages/mine/mine' })
  },

  goNextAfterLogin(userInfo = {}) {
    const returnUrl = this.data.returnUrl || ''
    if (isProfileComplete(userInfo)) {
      if (returnUrl) {
        wx.redirectTo({ url: returnUrl })
        return
      }
      this.goMine()
      return
    }
    const query = returnUrl ? `&returnUrl=${encodeURIComponent(returnUrl)}` : ''
    wx.redirectTo({
      url: `/pages/profile-edit/profile-edit?from=login${query}`
    })
  },

  goHome() {
    wx.switchTab({ url: '/pages/home/home' })
  },

  goMerchantEntry() {
    merchantEntry.openMerchantPortal()
  },

  preventMove() {}
})
