const mock = require('../../data/mock')
const util = require('../../utils/util')
const templateService = require('../../services/template')
const app = getApp()

Page({
  data: {
    productId: null,
    checkoutConfig: {},
    product: {},
    merchant: {},
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
    this.setData({ productId: parseInt(options.id || 101, 10) })
    this.loadData()
  },

  loadData() {
    const checkoutConfig = templateService.getTemplateSection('checkout')
    const product = mock.grouponList.find((item) => item.id === this.data.productId) || mock.grouponList[0]
    const merchant = mock.merchantList.find((item) => item.id === product.merchantId) || mock.merchantList[0]
    const couponList = mock.couponList.filter((item) => item.status === 'AVAILABLE')
    const phone = (app.globalData.userInfo && app.globalData.userInfo.phone) || mock.userInfo.phone

    this.setData(
      {
        checkoutConfig,
        product,
        merchant,
        useRuleList: [
          `有效期：${product.validPeriod}`,
          `预约说明：${product.bookingRule}`,
          `退款规则：${product.refundRule}`
        ],
        couponList,
        phone,
        couponIndex: 0,
        selectedCouponId: null,
        selectedCoupon: null
      },
      () => {
        this.syncCouponState()
      }
    )
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
    const base = this.data.product.price * this.data.quantity
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
        if (showThresholdToast) {
          util.showToast('当前数量未满足优惠券使用门槛')
        }
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
        : this.data.couponList.length
          ? '当前不使用优惠券'
          : '暂无可用优惠券',
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
    this.setData({ quantity }, () => {
      this.syncCouponState()
    })
  },

  onCouponChange(e) {
    const couponIndex = Number(e.detail.value)
    this.setData(
      {
        selectedCouponId: couponIndex === 0 ? 0 : (this.data.couponList[couponIndex - 1] || {}).couponId || 0
      },
      () => {
        this.syncCouponState(true)
      }
    )
  },

  handlePhoneInput(e) {
    this.setData({
      phone: e.detail.value
    })
  },

  submitOrder() {
    if (!app.needLogin()) return
    if (!this.data.phone) {
      util.showToast('请先填写手机号')
      return
    }
    const existing = util.getStoredOrderList(mock.orderList)
    const orderNo = util.generateOrderNo()
    const order = {
      id: Date.now(),
      orderNo,
      productId: this.data.product.id,
      merchantId: this.data.merchant.id,
      title: this.data.product.title,
      merchantName: this.data.merchant.name,
      image: this.data.product.image,
      quantity: this.data.quantity,
      orderAmount: this.data.product.price * this.data.quantity,
      couponAmount: this.data.selectedCoupon ? this.data.selectedCoupon.amount : 0,
      payAmount: this.data.payAmount,
      price: this.data.payAmount,
      phone: this.data.phone,
      status: 'PENDING_PAY',
      createTime: Date.now(),
      expireTime: Date.now() + 1000 * 60 * 15
    }

    util.setStoredOrderList([order, ...existing])
    util.showToast('订单已创建', 'success')
    util.setPendingOrderFilter('PENDING_PAY')
    setTimeout(() => {
      util.redirectTo(`/pages/order-detail/order-detail?orderNo=${orderNo}`)
    }, 400)
  }
})
