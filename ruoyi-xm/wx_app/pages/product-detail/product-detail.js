const util = require('../../utils/util')
const templateService = require('../../services/template')
const cartService = require('../../services/cart')
const api = require('../../api/index')

Page({
  data: {
    productId: null,
    productConfig: {},
    product: {},
    merchant: {},
    otherStoreList: [],
    decisionList: [],
    ruleList: [],
    serviceHighlightList: [],
    loading: true,
    isCollected: false
  },

  onLoad(options) {
    const id = parseInt(options.id, 10)
    if (!id || isNaN(id)) {
      util.showToast('商品不存在')
      setTimeout(() => util.navigateBack(), 1500)
      return
    }
    this.setData({ productId: id })
    this.loadProductDetail()
  },

  formatProduct(product = {}, productConfig = {}) {
    return {
      ...product,
      savingAmountText: (((product.originalPrice || 0) - (product.price || 0)) / 100).toFixed(0),
      bookingRequiredText: product.bookingRequired ? productConfig.bookingYesText : productConfig.bookingNoText
    }
  },

  loadProductDetail() {
    this.setData({ loading: true })
    const productConfig = templateService.getTemplateSection('productDetail')

    Promise.all([
      api.getGrouponDetail(this.data.productId),
      api.getMerchantList({})
    ]).then(([productData, merchantList]) => {
      const rawProduct = productData || {}
      const merchants = merchantList || []
      const merchant = merchants.find((item) => item.merchantId === rawProduct.merchantId) || merchants[0] || {}
      const product = this.formatProduct(rawProduct, productConfig)

      this.setData({
        productConfig,
        product,
        merchant,
        otherStoreList: [merchant],
        serviceHighlightList: rawProduct.tags || [],
        decisionList: [
          `原价 ¥${((rawProduct.originalPrice || 0) / 100).toFixed(2)}，优惠价 ¥${((rawProduct.price || 0) / 100).toFixed(2)}`,
          `已售 ${rawProduct.sales || 0}，库存 ${rawProduct.stock || 0}，有效期 ${rawProduct.validDays || 0} 天`,
          `适用门店 ${merchant.name || ''}`
        ],
        ruleList: [
          `有效期：${rawProduct.validPeriod || ''}`,
          productConfig.timeRangeRuleText || '',
          `是否预约：${product.bookingRequiredText || '无需预约'}`,
          `预约说明：${rawProduct.bookingRule || '无需预约'}`,
          `限购说明：${rawProduct.limitRule || '不限购'}`,
          `退款规则：${rawProduct.refundRule || '过期自动退款'}`
        ],
        loading: false
      })
    }).catch(() => {
      this.setData({
        productConfig,
        product: {},
        merchant: {},
        otherStoreList: [],
        serviceHighlightList: [],
        decisionList: [],
        ruleList: [],
        loading: false
      })
      util.showToast('加载失败，请重试')
    })
  },

  goMerchant() {
    util.navigateTo(`/pages/merchant-detail/merchant-detail?id=${this.data.merchant.id}`)
  },

  callMerchant() {
    if (!this.data.merchant.phone) {
      util.showToast('暂无联系电话')
      return
    }
    wx.makePhoneCall({
      phoneNumber: this.data.merchant.phone
    })
  },

  viewMerchantLocation() {
    const merchant = this.data.merchant
    wx.openLocation({
      latitude: merchant.latitude,
      longitude: merchant.longitude,
      name: merchant.name,
      address: merchant.address
    })
  },

  goHome() {
    util.switchTab('/pages/home/home')
  },

  toggleCollect() {
    this.setData({
      isCollected: !this.data.isCollected
    })
    util.showToast(this.data.isCollected ? '已收藏项目' : '已取消收藏', 'success')
  },

  shareProduct() {
    util.showToast('已唤起微信分享能力')
  },

  addToCart() {
    cartService.addToCart(this.data.product, 1)
    util.showToast('已加入购物车', 'success')
  },

  buyNow() {
    util.navigateTo(`/pages/checkout/checkout?id=${this.data.product.id}`)
  },

  onShareAppMessage() {
    return {
      title: this.data.product.title || '项目详情',
      path: `/pages/product-detail/product-detail?id=${this.data.productId}`
    }
  }
})
