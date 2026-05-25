const util = require('../../utils/util')
const api = require('../../api/index')

const app = getApp()

Page({
  data: {
    tabs: [
      { label: '优惠券', value: 'coupon' },
      { label: '满减活动', value: 'promotion' }
    ],
    currentTab: 'coupon',
    couponList: [],
    promotionList: [],
    showCouponForm: false,
    showPromotionForm: false,
    couponForm: {
      name: '',
      discountAmount: '',
      minOrderAmount: '',
      totalCount: '',
      validDays: '30'
    },
    promotionForm: {
      name: '',
      fullAmount: '',
      reduceAmount: ''
    }
  },

  onShow() {
    if (!app.needLogin()) return
    this.loadData()
  },

  loadData() {
    if (this.data.currentTab === 'coupon') {
      this.loadCoupons()
    } else {
      this.loadPromotions()
    }
  },

  loadCoupons() {
    api.getCouponList()
      .then((list = []) => {
        this.renderCoupons(list)
      })
      .catch(() => {
        util.showToast('加载失败，请重试')
      })
  },

  loadPromotions() {
    api.getPromotionList()
      .then((list = []) => {
        this.renderPromotions(list)
      })
      .catch(() => {
        util.showToast('加载失败，请重试')
      })
  },

  renderCoupons(list = []) {
    this.setData({
      couponList: list.map((item) => ({
        ...item,
        discountAmountText: util.formatPrice(item.discountAmount),
        minOrderAmountText: util.formatPrice(item.minOrderAmount),
        createTimeText: util.formatDate(item.createTime),
        remainCount: Math.max(0, (item.totalCount || 0) - (item.usedCount || 0))
      }))
    })
  },

  renderPromotions(list = []) {
    this.setData({
      promotionList: list.map((item) => ({
        ...item,
        fullAmountText: util.formatPrice(item.fullAmount),
        reduceAmountText: util.formatPrice(item.reduceAmount),
        createTimeText: util.formatDate(item.createTime)
      }))
    })
  },

  switchTab(e) {
    this.setData({ currentTab: e.currentTarget.dataset.tab }, () => this.loadData())
  },

  /* 优惠券 */
  openCouponForm() {
    this.setData({
      showCouponForm: true,
      couponForm: { name: '', discountAmount: '', minOrderAmount: '', totalCount: '', validDays: '30' }
    })
  },

  closeCouponForm() {
    this.setData({ showCouponForm: false })
  },

  handleCouponInput(e) {
    this.setData({ [`couponForm.${e.currentTarget.dataset.key}`]: e.detail.value })
  },

  submitCoupon() {
    const f = this.data.couponForm
    if (!f.name.trim()) { util.showToast('请输入优惠券名称'); return }
    if (!f.discountAmount) { util.showToast('请输入优惠金额'); return }

    const couponData = {
      name: f.name.trim(),
      discountAmount: Math.round(Number(f.discountAmount) * 100),
      minOrderAmount: Math.round(Number(f.minOrderAmount || 0) * 100),
      totalCount: Number(f.totalCount || 999),
      validDays: Number(f.validDays || 30)
    }

    api.saveCoupon(couponData)
      .then(() => {
        util.showToast('优惠券已创建', 'success')
        this.setData({ showCouponForm: false })
        this.loadData()
      })
      .catch(() => {
        util.showToast('创建失败，请重试')
      })
  },

  toggleCouponStatus(e) {
    const couponId = Number(e.currentTarget.dataset.id)
    util.showToast('状态切换功能需要后端支持')
  },

  /* 满减活动 */
  openPromotionForm() {
    this.setData({
      showPromotionForm: true,
      promotionForm: { name: '', fullAmount: '', reduceAmount: '' }
    })
  },

  closePromotionForm() {
    this.setData({ showPromotionForm: false })
  },

  handlePromotionInput(e) {
    this.setData({ [`promotionForm.${e.currentTarget.dataset.key}`]: e.detail.value })
  },

  submitPromotion() {
    const f = this.data.promotionForm
    if (!f.name.trim()) { util.showToast('请输入活动名称'); return }
    if (!f.fullAmount || !f.reduceAmount) { util.showToast('请输入满减金额'); return }

    const promoData = {
      name: f.name.trim(),
      fullAmount: Math.round(Number(f.fullAmount) * 100),
      reduceAmount: Math.round(Number(f.reduceAmount) * 100)
    }

    api.savePromotion(promoData)
      .then(() => {
        util.showToast('满减活动已创建', 'success')
        this.setData({ showPromotionForm: false })
        this.loadData()
      })
      .catch(() => {
        util.showToast('创建失败，请重试')
      })
  },

  togglePromotionStatus(e) {
    const promotionId = Number(e.currentTarget.dataset.id)
    util.showToast('状态切换功能需要后端支持')
  }
})
