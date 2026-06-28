const util = require('../../utils/util')
const templateService = require('../../services/template')
const productApi = require('../../api/product')
const merchantApi = require('../../api/merchant')
const { toDetailThumbnailUrl, toListThumbnailUrl } = require('../../utils/image-url')
const DEFAULT_PRODUCT_IMAGE = '/assets/images/merchant-logo-xiangyuan.png'
const DEFAULT_MERCHANT_IMAGE = '/assets/images/merchant-logo-xiangyuan.png'

Page({
  data: {
    productId: null,
    productConfig: {},
    product: {},
    merchant: {},
    decisionList: [],
    ruleList: [],
    serviceHighlightList: [],
    productImageCropStyle: util.buildImageCropStyle(),
    loading: true
  },

  onLoad(options) {
    this.setData({ productId: parseInt(options.id, 10) })
    this.loadProductDetail()
  },

  formatProduct(product = {}, productConfig = {}) {
    const price = product.price || 0
    const originalPrice = product.originalPrice || 0
    return {
      ...product,
      title: product.title || product.name || '',
      image: toDetailThumbnailUrl(product.image || product.coverImage || product.mainImage || DEFAULT_PRODUCT_IMAGE),
      soldOut: Number(product.stock || 0) <= 0,
      priceText: (price / 100).toFixed(2),
      originalPriceText: (originalPrice / 100).toFixed(2),
      savingAmountText: ((Math.max(originalPrice - price, 0)) / 100).toFixed(0),
      bookingRequiredText: product.bookingRequired ? productConfig.bookingYesText : productConfig.bookingNoText
    }
  },

  formatMerchant(merchant = {}) {
    return {
      ...merchant,
      name: merchant.name || merchant.shortName || '门店信息待完善',
      address: merchant.address || '门店地址待完善',
      businessHours: merchant.businessHours || merchant.businessHoursText || '营业时间待完善',
      distance: merchant.distance || '',
      avatar: toListThumbnailUrl(merchant.avatar || merchant.coverImage || DEFAULT_MERCHANT_IMAGE)
    }
  },

  loadProductDetail() {
    this.setData({ loading: true })

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
            `核销说明：${rawProduct.verifyNotice || '到店出示核销码即可使用'}`,
            `限购说明：${rawProduct.limitRule || '不限购'}`,
            `退款规则：${rawProduct.refundRule || '过期自动退款'}`
          ]
        })

        if (rawProduct.merchantId) {
          merchantApi.getMerchantDetail(rawProduct.merchantId)
            .then((merchantRes) => {
              const merchant = this.formatMerchant(merchantRes.data || merchantRes || {})
              this.setData({
                merchant,
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

  buyNow() {
    if (this.data.product.soldOut || Number(this.data.product.stock || 0) <= 0) {
      util.showToast('当前商品已售罄')
      return
    }
    util.navigateTo(`/pages/checkout/checkout?id=${this.data.product.id}`)
  },

  onShareAppMessage() {
    return {
      title: this.data.product.title || '项目详情',
      path: `/pages/product-detail/product-detail?id=${this.data.productId}`
    }
  }
})
