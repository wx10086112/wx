const mock = require('../../data/mock')
const util = require('../../utils/util')
const templateService = require('../../services/template')
const cartService = require('../../services/cart')
<<<<<<< HEAD
=======
const productApi = require('../../api/product')
const merchantApi = require('../../api/merchant')
>>>>>>> 苏

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
    productImageCropStyle: util.buildImageCropStyle(),
    loading: true,
    isCollected: false
  },

  onLoad(options) {
<<<<<<< HEAD
    this.setData({ productId: parseInt(options.id || 101, 10) })
=======
    this.setData({ productId: parseInt(options.id, 10) })
>>>>>>> 苏
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
<<<<<<< HEAD
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
          `原价 ¥${(rawProduct.originalPrice / 100).toFixed(2)}，优惠价 ¥${(rawProduct.price / 100).toFixed(2)}`,
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
=======

    const productConfig = templateService.getTemplateSection('productDetail')

    productApi.getGrouponDetail(this.data.productId)
      .then((res) => {
        const rawProduct = res.data || res || {}
        const product = this.formatProduct(rawProduct, productConfig)

        this.setData({
          productConfig,
          product,
          serviceHighlightList: rawProduct.tags || [],
          decisionList: [
            `原价 ¥${((rawProduct.originalPrice || 0) / 100).toFixed(2)}，优惠价 ¥${((rawProduct.price || 0) / 100).toFixed(2)}`,
            `已售 ${rawProduct.sales || 0}，库存 ${rawProduct.stock || 0}，有效期 ${rawProduct.validDays || 0} 天`
          ],
          ruleList: [
            `有效期：${rawProduct.validPeriod || '购买后有效'}`,
            productConfig.timeRangeRuleText,
            `是否预约：${product.bookingRequiredText}`,
            `预约说明：${rawProduct.bookingRule || '无需预约'}`,
            `限购说明：${rawProduct.limitRule || '不限购'}`,
            `退款规则：${rawProduct.refundRule || '过期自动退款'}`
          ]
        })

        // 加载商家信息
        if (rawProduct.merchantId) {
          merchantApi.getMerchantDetail(rawProduct.merchantId)
            .then((merchantRes) => {
              const merchant = merchantRes.data || merchantRes || {}
              this.setData({
                merchant,
                otherStoreList: [merchant],
                loading: false
              })
            })
            .catch(() => {
              this.setData({ loading: false })
            })
        } else {
          this.setData({ loading: false })
        }
      })
      .catch(() => {
        this.setData({ loading: false })
        util.showToast('加载失败，请重试')
      })
>>>>>>> 苏
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
    if (!merchant.latitude || !merchant.longitude) {
      util.showToast('暂无门店位置')
      return
    }
    wx.openLocation({
      latitude: Number(merchant.latitude),
      longitude: Number(merchant.longitude),
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
