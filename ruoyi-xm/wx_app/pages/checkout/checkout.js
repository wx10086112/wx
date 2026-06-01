const mock = require('../../data/mock')
const util = require('../../utils/util')
const templateService = require('../../services/template')
const cartService = require('../../services/cart')
const orderApi = require('../../api/order')
const productApi = require('../../api/product')
const merchantApi = require('../../api/merchant')
const app = getApp()

Page({
  data: {
    checkoutMode: 'single',
    cartIds: [],
    productId: null,
    checkoutConfig: {},
    product: {},
    merchant: {},
    cartItemList: [],
    quantity: 1,
    phone: '',
    couponList: [],
    couponOptionList: ['不使用优惠券'],
    couponIndex: 0,
    selectedCouponId: null,
    selectedCoupon: null,
    couponHintText: '',
    couponAmountText: '0.00',
    subtotalText: '0.00',
    payAmountText: '0.00',
    payAmount: 0,
    useRuleList: []
  },

  onLoad(options) {
    if (options.cartIds) {
      const cartIds = options.cartIds.split(',').map(Number)
      this.setData({ checkoutMode: 'cart', cartIds })
    } else {
      this.setData({ checkoutMode: 'single', productId: parseInt(options.id, 10) })
    }
    this.loadData()
  },

  loadData() {
    const checkoutConfig = templateService.getTemplateSection('checkout')
    const couponList = mock.couponList.filter((item) => item.status === 'AVAILABLE')
    const phone = (app.globalData.userInfo && app.globalData.userInfo.phone) || mock.userInfo.phone

    if (this.data.checkoutMode === 'cart') {
      const cartItems = cartService.getSelectedItems()
      const cartItemList = cartItems.map((item) => ({
        ...item,
        priceText: (item.price / 100).toFixed(2)
      }))
      const totalAmount = cartItems.reduce((sum, item) => sum + item.price * item.quantity, 0)
      const merchantName = cartItems.length === 1 ? cartItems[0].merchantName : `${cartItems.length} 件商品`

      this.setData(
        {
          checkoutConfig,
          cartItemList,
          product: { title: merchantName, price: totalAmount, stock: 999 },
          merchant: { name: merchantName },
          useRuleList: ['支付成功后自动生成使用码，可在订单中心随时查看'],
          couponList,
          phone,
          couponIndex: 0,
          selectedCouponId: null,
          selectedCoupon: null
        },
        () => { this.syncCouponState() }
      )
    } else {
      productApi.getGrouponDetail(this.data.productId)
        .then((res) => {
          const product = res.data || res || {}
          const merchantId = product.merchantId

          const ruleList = [
            `有效期：${product.validPeriod || '购买后有效'}`,
            `预约说明：${product.bookingRule || '无需预约'}`,
            `退款规则：${product.refundRule || '过期自动退款'}`
          ]

          const applyMerchant = (merchantData) => {
            this.setData(
              {
                checkoutConfig,
                product,
                merchant: merchantData || {},
                useRuleList: ruleList,
                couponList: [],
                phone,
                couponIndex: 0,
                selectedCouponId: null,
                selectedCoupon: null
              },
              () => { this.syncCouponState() }
            )
          }

          if (merchantId) {
            merchantApi.getMerchantDetail(merchantId)
              .then((merchantRes) => {
                applyMerchant(merchantRes.data || merchantRes || {})
              })
              .catch(() => { applyMerchant() })
          } else {
            applyMerchant()
          }
        })
        .catch(() => {
          util.showToast('商品加载失败')
        })
    }
  },

  getBaseAmount() {
    if (this.data.checkoutMode === 'cart') {
      return this.data.cartItemList.reduce((sum, item) => sum + item.price * item.quantity, 0)
    }
    return (this.data.product.price || 0) * this.data.quantity
  },

  getCouponOptionLabel(coupon, baseAmount) {
    const thresholdText = (coupon.thresholdAmount / 100).toFixed(0)
    if (baseAmount >= coupon.thresholdAmount) {
      return `${coupon.couponName} -${(coupon.amount / 100).toFixed(0)}元`
    }
    return `${coupon.couponName} (满${thresholdText}元可用)`
  },

  getBestCoupon(baseAmount) {
    return this.data.couponList
      .filter((item) => baseAmount >= item.thresholdAmount)
      .sort((a, b) => b.amount - a.amount)[0]
  },

  syncCouponState(showThresholdToast = false) {
    const base = this.getBaseAmount()
    const couponOptionList = ['不使用优惠券'].concat(
      this.data.couponList.map((item) => this.getCouponOptionLabel(item, base))
    )

    let selectedCoupon = null
    let selectedCouponId = this.data.selectedCouponId
    let couponIndex = 0

    if (selectedCouponId === null) {
      selectedCoupon = this.getBestCoupon(base) || null
      selectedCouponId = selectedCoupon ? selectedCoupon.couponId : 0
    } else if (selectedCouponId > 0) {
      const preferredCoupon = this.data.couponList.find((item) => item.couponId === selectedCouponId)
      if (preferredCoupon && base >= preferredCoupon.thresholdAmount) {
        selectedCoupon = preferredCoupon
      } else {
        if (showThresholdToast) util.showToast('当前金额未满足优惠券使用门槛')
        selectedCouponId = 0
      }
    }

    if (selectedCoupon) {
      couponIndex = this.data.couponList.findIndex((item) => item.couponId === selectedCoupon.couponId) + 1
    }

    const couponAmount = selectedCoupon ? selectedCoupon.amount : 0
    const payAmount = Math.max(base - couponAmount, 0)

    this.setData({
      couponOptionList,
      couponIndex,
      selectedCouponId,
      selectedCoupon,
      couponHintText: selectedCoupon
        ? `已抵扣 ¥${(selectedCoupon.amount / 100).toFixed(2)}`
        : this.data.couponList.length ? '当前不使用优惠券' : '暂无可用优惠券',
      payAmount,
      couponAmountText: (couponAmount / 100).toFixed(2),
      subtotalText: (base / 100).toFixed(2),
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
    this.setData({ quantity }, () => { this.syncCouponState() })
  },

  onCouponChange(e) {
    const couponIndex = Number(e.detail.value)
    this.setData(
      { selectedCouponId: couponIndex === 0 ? 0 : (this.data.couponList[couponIndex - 1] || {}).couponId || 0 },
      () => { this.syncCouponState(true) }
    )
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
        if (this.data.checkoutMode === 'cart') cartService.removeSelected()
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
    if (this.data.checkoutMode === 'cart') {
      return {
        items: this.data.cartItemList.map((item) => ({
          productId: item.productId,
          quantity: item.quantity
        })),
        phone: this.data.phone,
        couponId: this.data.selectedCoupon ? this.data.selectedCoupon.couponId : null
      }
    }
    return {
      productId: this.data.product.id,
      quantity: this.data.quantity,
      phone: this.data.phone,
      couponId: this.data.selectedCoupon ? this.data.selectedCoupon.couponId : null
    }
  }
})
