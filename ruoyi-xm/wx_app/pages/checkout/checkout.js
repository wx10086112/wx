const util = require('../../utils/util')
const templateService = require('../../services/template')
const orderApi = require('../../api/order')
const productApi = require('../../api/product')
const merchantApi = require('../../api/merchant')
const userApi = require('../../api/user')
const agreement = require('../../utils/agreement')
const { toListThumbnailUrl } = require('../../utils/image-url')
const app = getApp()
const DEFAULT_PRODUCT_IMAGE = '/assets/images/merchant-logo-xiangyuan.png'
const LOGIN_EXPIRED_TEXT = '登录已过期'

const buildCheckoutReturnUrl = (productId) => {
  return `/pages/checkout/checkout?id=${productId}`
}

const normalizeUserInfo = (info = {}) => {
  return {
    ...(app.globalData.userInfo || {}),
    ...info,
    nickName: info.nickName || info.userName || (app.globalData.userInfo && app.globalData.userInfo.nickName) || '',
    avatarUrl: info.avatarUrl || (app.globalData.userInfo && app.globalData.userInfo.avatarUrl) || '',
    phone: info.phone || ''
  }
}

Page({
  data: {
    productId: null,
    checkoutConfig: {},
    product: {},
    merchant: {},
    quantity: 1,
    phone: '',
    phoneBound: false,
    subtotalText: '0.00',
    payAmountText: '0.00',
    payAmount: 0,
    useRuleList: []
  },

  onLoad(options) {
    const productId = parseInt(options.id, 10)
    if (!productId) {
      util.showToast('商品不存在')
      setTimeout(() => { wx.navigateBack() }, 500)
      return
    }
    this.setData({ productId })
    this.loadData()
  },

  onShow() {
    this.syncPhoneState()
    this.refreshUserInfo()
  },

  syncPhoneState(phoneValue) {
    const phone = String(phoneValue !== undefined
      ? phoneValue
      : (app.globalData.userInfo && app.globalData.userInfo.phone) || '').trim()
    this.setData({
      phone,
      phoneBound: /^1\d{10}$/.test(phone)
    })
  },

  refreshUserInfo() {
    const token = wx.getStorageSync('token')
    if (!token || !app.globalData.isLoggedIn) return Promise.resolve()
    return userApi.getUserInfo()
      .then((res = {}) => {
        const payload = res.data || res || {}
        const userInfo = normalizeUserInfo(payload)
        app.setLoginInfo(app.globalData.token || token, userInfo)
        this.syncPhoneState(userInfo.phone)
      })
      .catch(() => {})
  },

  loadData() {
    const checkoutConfig = templateService.getTemplateSection('checkout')
    const phone = String((app.globalData.userInfo && app.globalData.userInfo.phone) || '').trim()

    productApi.getGrouponDetail(this.data.productId)
      .then((res) => {
        const product = this.formatProduct(res.data || res || {})
        const merchantId = product.merchantId
        const useRuleList = [
          `有效期：${product.validPeriod || '购买后有效'}`,
          `核销说明：${product.verifyNotice || '到店出示核销码即可使用'}`,
          `预约说明：${product.bookingRule || '无需预约'}`,
          `退款规则：${product.refundRule || '按商家规则处理'}`
        ]

        const applyData = (merchantData) => {
          this.setData({
            checkoutConfig,
            product,
            merchant: this.formatMerchant(merchantData || {}),
            useRuleList,
            phone,
            phoneBound: /^1\d{10}$/.test(phone)
          }, () => { this.syncPriceState() })
        }

        if (merchantId) {
          merchantApi.getMerchantDetail(merchantId)
            .then((merchantRes) => { applyData(merchantRes.data || merchantRes || {}) })
            .catch(() => { applyData() })
        } else {
          applyData()
        }
      })
      .catch(() => {
        util.showToast('商品加载失败')
      })
  },

  formatProduct(product = {}) {
    const price = product.price || 0
    return {
      ...product,
      title: product.title || product.name || '',
      image: toListThumbnailUrl(product.image || product.coverImage || product.mainImage || DEFAULT_PRODUCT_IMAGE),
      soldOut: Number(product.stock || 0) <= 0,
      priceText: (price / 100).toFixed(2)
    }
  },

  formatMerchant(merchant = {}) {
    return {
      ...merchant,
      name: merchant.name || merchant.shortName || '门店信息待完善',
      shortName: merchant.shortName || merchant.name || '门店信息待完善',
      distance: merchant.distance || ''
    }
  },

  syncPriceState() {
    const payAmount = (this.data.product.price || 0) * this.data.quantity
    this.setData({
      payAmount,
      subtotalText: (payAmount / 100).toFixed(2),
      payAmountText: (payAmount / 100).toFixed(2)
    })
  },

  changeQuantity(e) {
    const delta = Number(e.currentTarget.dataset.delta)
    const nextQuantity = this.data.quantity + delta
    const quantity = Math.max(1, Math.min(nextQuantity, this.data.product.stock || nextQuantity))
    if (delta > 0 && quantity === this.data.quantity) {
      util.showToast('已达到库存上限')
      return
    }
    this.setData({ quantity }, () => { this.syncPriceState() })
  },

  goBindPhone() {
    const returnUrl = encodeURIComponent(`/pages/checkout/checkout?id=${this.data.productId}`)
    util.navigateTo(`/pages/profile-edit/profile-edit?from=checkout&returnUrl=${returnUrl}`)
  },

  goLogin() {
    const returnUrl = encodeURIComponent(buildCheckoutReturnUrl(this.data.productId))
    util.navigateTo(`/pages/login/login?returnUrl=${returnUrl}`)
  },

  ensureLoginBeforeSubmit() {
    const token = wx.getStorageSync('token')
    if (app.globalData.isLoggedIn && token) {
      return true
    }
    if (app.clearLoginInfo) {
      app.clearLoginInfo()
    }
    util.showToast('请先登录')
    setTimeout(() => {
      this.goLogin()
    }, 500)
    return false
  },

  submitOrder() {
    if (!this.ensureLoginBeforeSubmit()) return
    if (!agreement.assertAgreementAccepted()) return
    this.refreshUserInfo().then(() => {
      this.submitOrderWithReadyProfile()
    })
  },

  submitOrderWithReadyProfile() {
    const phone = String(this.data.phone || '').trim()
    if (this.data.product.soldOut || Number(this.data.product.stock || 0) <= 0) {
      util.showToast('当前商品已售罄')
      return
    }
    if (!/^1\d{10}$/.test(phone)) {
      util.showToast('请先授权手机号')
      this.goBindPhone()
      return
    }

    util.showLoading('提交中...')

    const apiPayload = this.buildApiPayload()
    orderApi
      .createOrder(apiPayload)
      .then((res) => {
        util.hideLoading()
        const orderNo = res.data ? res.data.orderNo : res.orderNo
        util.showToast('订单已创建', 'success')
        util.setPendingOrderFilter('PENDING_PAY')
        setTimeout(() => {
          util.redirectTo(`/pages/order-detail/order-detail?orderNo=${orderNo}`)
        }, 400)
      })
      .catch((err) => {
        util.hideLoading()
        const message = (err && (err.message || err.msg)) || '提交失败，请重试'
        util.showToast(message)
        if (message.includes(LOGIN_EXPIRED_TEXT)) {
          setTimeout(() => {
            this.goLogin()
          }, 500)
        }
      })
  },

  buildApiPayload() {
    return {
      productId: this.data.product.id || this.data.product.goodsId || this.data.productId,
      quantity: this.data.quantity,
      phone: String(this.data.phone || '').trim()
    }
  }
})
