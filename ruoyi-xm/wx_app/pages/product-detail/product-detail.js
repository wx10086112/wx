const mock = require('../../data/mock')
const util = require('../../utils/util')
const templateService = require('../../services/template')

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
    this.setData({ productId: parseInt(options.id || 101, 10) })
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
    setTimeout(() => {
      const productConfig = templateService.getTemplateSection('productDetail')
      const rawProduct = mock.grouponList.find((item) => item.id === this.data.productId) || mock.grouponList[0]
      const merchant = mock.merchantList.find((item) => item.id === rawProduct.merchantId) || mock.merchantList[0]
      const otherStoreList = [merchant]
      const product = this.formatProduct(rawProduct, productConfig)

      this.setData({
        productConfig,
        product,
        merchant,
        otherStoreList,
        serviceHighlightList: rawProduct.tags || [],
        decisionList: [
          `原价 ¥${(rawProduct.originalPrice / 100).toFixed(2)}，团购价 ¥${(rawProduct.price / 100).toFixed(2)}`,
          `已售 ${rawProduct.sales}，库存 ${rawProduct.stock}，有效期 ${rawProduct.validDays} 天`,
          `适用门店 ${merchant.name}，距您约 ${merchant.distance}`
        ],
        ruleList: [
          `有效期：${rawProduct.validPeriod}`,
          productConfig.timeRangeRuleText,
          `是否预约：${product.bookingRequiredText}`,
          `预约说明：${rawProduct.bookingRule}`,
          `限购说明：${rawProduct.limitRule}`,
          `退款规则：${rawProduct.refundRule}`
        ],
        loading: false
      })
    }, 150)
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
    util.showToast(this.data.isCollected ? '已收藏套餐' : '已取消收藏', 'success')
  },

  shareProduct() {
    util.showToast('已唤起微信分享能力')
  },

  buyNow() {
    util.navigateTo(`/pages/checkout/checkout?id=${this.data.product.id}`)
  },

  onShareAppMessage() {
    return {
      title: this.data.product.title || '套餐详情',
      path: `/pages/product-detail/product-detail?id=${this.data.productId}`
    }
  }
})
