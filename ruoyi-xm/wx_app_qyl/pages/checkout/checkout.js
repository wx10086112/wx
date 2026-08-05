const util = require('../../utils/util')
const templateService = require('../../services/template')
const orderApi = require('../../api/order')
const productApi = require('../../api/product')
const merchantApi = require('../../api/merchant')
const userApi = require('../../api/user')
const agreement = require('../../utils/agreement')
const cartStore = require('../../utils/cart')
const cartSync = require('../../utils/cart-sync')
const { toListThumbnailUrl } = require('../../utils/image-url')
const app = getApp()
const DEFAULT_PRODUCT_IMAGE = '/assets/images/merchant-logo-xiangyuan.png'
const LOGIN_EXPIRED_TEXT = '登录已过期'

const buildCheckoutReturnUrl = (productId, fromCart) => {
  return fromCart ? '/pages/checkout/checkout?cart=1' : `/pages/checkout/checkout?id=${productId}`
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
    fromCart: false,
    checkoutConfig: {},
    product: {},
    productList: [],
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
    const fromCart = options.cart === '1'
    const productId = parseInt(options.id, 10)
    if (!fromCart && !productId) {
      util.showToast('商品不存在')
      setTimeout(() => { wx.navigateBack() }, 500)
      return
    }
    this.setData({ productId, fromCart })
    this.skipNextShowRefresh = true
    this.loadData()
  },

  onShow() {
    this.syncPhoneState()
    this.refreshUserInfo()
    if (this.skipNextShowRefresh) {
      this.skipNextShowRefresh = false
      return
    }
    if (this.data.fromCart) {
      this.loadCartCheckout()
    } else if (this.data.productId) {
      this.loadSingleCheckout()
    }
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
    if (this.data.fromCart) {
      this.loadCartCheckout()
      return
    }
    this.loadSingleCheckout()
  },

  loadSingleCheckout() {
    const checkoutConfig = templateService.getTemplateSection('checkout')
    const phone = String((app.globalData.userInfo && app.globalData.userInfo.phone) || '').trim()

    productApi.getGrouponDetail(this.data.productId)
      .then((res) => {
        const product = this.formatProduct(res.data || res || {})
        const productList = [this.formatCheckoutItem(product, 1)]
        const merchantId = product.merchantId
        const useRuleList = this.buildUseRuleList(product)

        const applyData = (merchantData) => {
          this.setData({
            checkoutConfig,
            product,
            productList,
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

  loadCartCheckout() {
    const checkoutConfig = templateService.getTemplateSection('checkout')
    const phone = String((app.globalData.userInfo && app.globalData.userInfo.phone) || '').trim()
    return cartSync.refreshCart()
      .catch(() => cartStore.getCart())
      .then((cart) => {
        if (!cart.items.length) {
          util.showToast('购物车为空')
          setTimeout(() => { util.redirectTo('/pages/cart/cart') }, 500)
          return null
        }

        const productList = cart.items.map((item) => this.formatCheckoutItem(item, item.quantity))
        const firstProduct = productList[0] || {}
        const product = {
          ...firstProduct,
          id: firstProduct.entryId || firstProduct.productId,
          title: productList.length > 1 ? `${firstProduct.title}等${productList.length}件商品` : firstProduct.title,
          soldOut: productList.some((item) => item.soldOut)
        }
        const useRuleList = [
          '有效期：按各商品详情页说明执行',
          '核销说明：支付成功后生成同一订单使用码，到店出示核销',
          '预点单说明：如商品要求预点单，请按门店规则提前联系',
          '退款规则：按商家规则处理'
        ]

        const applyData = (merchantData) => {
          this.setData({
            checkoutConfig,
            product,
            productList,
            merchant: this.formatMerchant({
              ...(merchantData || {}),
              id: cart.merchantId,
              name: (merchantData && merchantData.name) || cart.merchantName,
              shortName: (merchantData && merchantData.shortName) || cart.merchantName
            }),
            useRuleList,
            phone,
            phoneBound: /^1\d{10}$/.test(phone)
          }, () => { this.syncPriceState() })
        }

        if (cart.merchantId) {
          return merchantApi.getMerchantDetail(cart.merchantId)
            .then((merchantRes) => { applyData(merchantRes.data || merchantRes || {}) })
            .catch(() => { applyData() })
        }
        applyData()
        return null
      })
  },

  buildUseRuleList(product = {}) {
    return [
      `有效期：${product.validPeriod || '购买后有效'}`,
      `核销说明：${product.verifyNotice || '到店出示核销码即可使用'}`,
      `预点单说明：${product.bookingRule || '无需预点单'}`,
      `退款规则：${product.refundRule || '按商家规则处理'}`
    ]
  },

  formatProduct(product = {}) {
    const price = product.price || 0
    return {
      ...product,
      productId: product.productId || product.id || product.goodsId,
      title: product.title || product.name || '',
      image: toListThumbnailUrl(product.image || product.coverImage || product.mainImage || DEFAULT_PRODUCT_IMAGE),
      soldOut: Number(product.stock || 0) <= 0,
      price,
      priceText: (price / 100).toFixed(2)
    }
  },

  formatCheckoutItem(item = {}, quantity = 1) {
    const price = Number(item.price || 0)
    const itemQuantity = Math.max(1, Number(quantity || item.quantity || 1))
    const stock = Number(item.stock || 0)
    return {
      ...item,
      entryId: item.entryId || item.id || item.goodsId || item.productId,
      productId: item.productId || item.id || item.goodsId,
      title: item.title || item.name || item.productName || '',
      image: toListThumbnailUrl(item.image || item.coverImage || item.mainImage || DEFAULT_PRODUCT_IMAGE),
      price,
      quantity: itemQuantity,
      stock,
      soldOut: stock <= 0 || itemQuantity > stock,
      priceText: (price / 100).toFixed(2),
      subtotalText: ((price * itemQuantity) / 100).toFixed(2)
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
    const payAmount = this.data.productList.reduce((sum, item) => {
      return sum + Number(item.price || 0) * Number(item.quantity || 0)
    }, 0)
    this.setData({
      payAmount,
      subtotalText: (payAmount / 100).toFixed(2),
      payAmountText: (payAmount / 100).toFixed(2)
    })
  },

  changeQuantity(e) {
    if (this.data.fromCart) {
      this.goCart()
      return
    }
    const delta = Number(e.currentTarget.dataset.delta)
    const nextQuantity = this.data.quantity + delta
    const quantity = Math.max(1, Math.min(nextQuantity, this.data.product.stock || nextQuantity))
    if (delta > 0 && quantity === this.data.quantity) {
      util.showToast('已达到库存上限')
      return
    }
    const productList = this.data.productList.map((item) => ({
      ...item,
      quantity,
      subtotalText: ((Number(item.price || 0) * quantity) / 100).toFixed(2)
    }))
    this.setData({ quantity, productList }, () => { this.syncPriceState() })
  },

  goBindPhone() {
    const returnUrl = encodeURIComponent(buildCheckoutReturnUrl(this.data.productId, this.data.fromCart))
    util.navigateTo(`/pages/profile-edit/profile-edit?from=checkout&returnUrl=${returnUrl}`)
  },

  goCart() {
    util.navigateTo('/pages/cart/cart')
  },

  goLogin() {
    const returnUrl = encodeURIComponent(buildCheckoutReturnUrl(this.data.productId, this.data.fromCart))
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
    if (!this.data.productList.length) {
      util.showToast('请先选择商品')
      return
    }
    if (this.data.productList.some((item) => item.soldOut)) {
      util.showToast('部分商品库存不足')
      return
    }
    if (!/^1\d{10}$/.test(phone)) {
      util.showToast('请先授权手机号')
      this.goBindPhone()
      return
    }

    util.showLoading('提交中...')

    orderApi
      .createOrder(this.buildApiPayload())
      .then((res) => {
        util.hideLoading()
        const orderNo = res.data ? res.data.orderNo : res.orderNo
        if (this.data.fromCart) {
          cartStore.clearCart()
        }
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
      productId: this.data.productList[0] && this.data.productList[0].productId,
      quantity: this.data.productList[0] && this.data.productList[0].quantity,
      items: this.data.productList.map((item) => ({
        productId: item.productId,
        quantity: item.quantity
      })),
      phone: String(this.data.phone || '').trim()
    }
  }
})
