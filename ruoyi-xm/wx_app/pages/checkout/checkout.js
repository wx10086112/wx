const util = require('../../utils/util')
const templateService = require('../../services/template')
const orderApi = require('../../api/order')
const productApi = require('../../api/product')
const merchantApi = require('../../api/merchant')
const app = getApp()

Page({
  data: {
    productId: null,
    checkoutConfig: {},
    product: {},
    merchant: {},
    quantity: 1,
    phone: '',
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

  loadData() {
    const checkoutConfig = templateService.getTemplateSection('checkout')
    const phone = (app.globalData.userInfo && app.globalData.userInfo.phone) || ''

    productApi.getGrouponDetail(this.data.productId)
      .then((res) => {
        const product = this.formatProduct(res.data || res || {})
        const merchantId = product.merchantId
        const useRuleList = [
          `有效期：${product.validPeriod || '购买后有效'}`,
          `预约说明：${product.bookingRule || '无需预约'}`,
          `退款规则：${product.refundRule || '按商家规则处理'}`
        ]

        const applyData = (merchantData) => {
          this.setData({
            checkoutConfig,
            product,
            merchant: merchantData || {},
            useRuleList,
            phone
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
      image: product.image || product.coverImage || product.mainImage || '',
      priceText: (price / 100).toFixed(2)
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

  handlePhoneInput(e) {
    this.setData({ phone: e.detail.value })
  },

  submitOrder() {
    if (!app.needLogin()) return
    if (!this.data.phone) {
      util.showToast('请先填写手机号')
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
        util.showToast(err && err.msg ? err.msg : '提交失败，请重试')
      })
  },

  buildApiPayload() {
    return {
      productId: this.data.product.id,
      quantity: this.data.quantity,
      phone: this.data.phone
    }
  }
})
