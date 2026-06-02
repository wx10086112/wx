const api = require('../../../api/merchant-mini/index')
const util = require('../../../utils/merchant-util')

const app = getApp()

Page({
  data: {
    merchantId: null,
    merchantEntry: null,
    entryLoading: false,
    username: '',
    password: '',
    submitting: false
  },

  onLoad(options = {}) {
    if (app.globalData.isMerchantLoggedIn) {
      wx.redirectTo({
        url: '/pages/merchant/workbench/workbench'
      })
      return
    }

    this.initMerchantEntry(options)
  },

  initMerchantEntry(options = {}) {
    const optionMerchantId = Number(options.merchantId || 0)
    const cachedEntry = app.getMerchantEntry ? app.getMerchantEntry() : null
    const merchantId = optionMerchantId || Number((cachedEntry && cachedEntry.merchantId) || 0)

    if (!merchantId) {
      this.setData({
        merchantId: null,
        merchantEntry: null
      })
      return
    }

    const nextEntry = app.setMerchantEntry({ ...(cachedEntry || {}), merchantId })
    this.setData({
      merchantId,
      merchantEntry: nextEntry
    })
    this.loadMerchantEntryInfo(merchantId)
  },

  loadMerchantEntryInfo(merchantId) {
    this.setData({ entryLoading: true })
    api
      .getMerchantEntryInfo(merchantId)
      .then((response = {}) => {
        const merchantEntry = app.setMerchantEntry({
          merchantId,
          merchantName: response.merchantName,
          contact: response.contact,
          phone: response.phone,
          loginPage: response.loginPage
        })
        this.setData({
          merchantId,
          merchantEntry
        })
      })
      .catch((err) => {
        util.showToast(err.message || '商家入口信息加载失败')
      })
      .finally(() => {
        this.setData({ entryLoading: false })
      })
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
    const username = (this.data.username || '').trim()
    const password = (this.data.password || '').trim()
    const merchantId = Number(this.data.merchantId || 0)

    if (!merchantId) {
      util.showToast('请先通过商家后台入口码进入')
      return
    }

    if (!username) {
      util.showToast('请输入登录账号')
      return
    }
    if (!password) {
      util.showToast('请输入登录密码')
      return
    }

    this.setData({ submitting: true })
    util.showLoading('登录中...')

    api
      .merchantLogin({
        username,
        password,
        merchantId
      })
      .then((response) => {
        app.setMerchantLoginInfo(response.token, response.staffUser)
        util.showToast('登录成功', 'success')
        wx.redirectTo({
          url: '/pages/merchant/workbench/workbench'
        })
      })
      .catch((err) => {
        util.showToast(err.message || '登录失败，请检查账号密码')
      })
      .finally(() => {
        this.setData({ submitting: false })
        util.hideLoading()
      })
  }
})
