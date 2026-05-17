const util = require('../../utils/util')
const api = require('../../api/index')

const app = getApp()

const COUPON_KEY = 'merchant_coupon_list'
const PROMOTION_KEY = 'merchant_promotion_list'

const getLocalCoupons = () => JSON.parse(JSON.stringify(wx.getStorageSync(COUPON_KEY) || []))
const setLocalCoupons = (list) => wx.setStorageSync(COUPON_KEY, JSON.parse(JSON.stringify(list)))
const getLocalPromotions = () => JSON.parse(JSON.stringify(wx.getStorageSync(PROMOTION_KEY) || []))
const setLocalPromotions = (list) => wx.setStorageSync(PROMOTION_KEY, JSON.parse(JSON.stringify(list)))

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
        setLocalCoupons(list)
        this.renderCoupons(list)
      })
      .catch(() => {
        this.renderCoupons(this.initMockCoupons())
      })
  },

  loadPromotions() {
    api.getPromotionList()
      .then((list = []) => {
        setLocalPromotions(list)
        this.renderPromotions(list)
      })
      .catch(() => {
        this.renderPromotions(this.initMockPromotions())
      })
  },

  initMockCoupons() {
    let list = getLocalCoupons()
    if (!list.length) {
      list = [
        {
          couponId: 1,
          name: '新客立减 10 元券',
          discountAmount: 1000,
          minOrderAmount: 5000,
          totalCount: 200,
          usedCount: 56,
          validDays: 30,
          status: 'ACTIVE',
          createTime: Date.now() - 86400000 * 5
        },
        {
          couponId: 2,
          name: '满 100 减 15 券',
          discountAmount: 1500,
          minOrderAmount: 10000,
          totalCount: 100,
          usedCount: 23,
          validDays: 15,
          status: 'ACTIVE',
          createTime: Date.now() - 86400000 * 2
        }
      ]
      setLocalCoupons(list)
    }
    return list
  },

  initMockPromotions() {
    let list = getLocalPromotions()
    if (!list.length) {
      list = [
        {
          promotionId: 1,
          name: '午市满减',
          fullAmount: 8000,
          reduceAmount: 1000,
          status: 'ACTIVE',
          createTime: Date.now() - 86400000 * 3
        }
      ]
      setLocalPromotions(list)
    }
    return list
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
        const list = getLocalCoupons()
        const maxId = list.reduce((max, item) => Math.max(max, item.couponId || 0), 0)
        list.unshift({
          couponId: maxId + 1,
          ...couponData,
          usedCount: 0,
          status: 'ACTIVE',
          createTime: Date.now()
        })
        setLocalCoupons(list)
        util.showToast('已本地创建优惠券', 'success')
        this.setData({ showCouponForm: false })
        this.renderCoupons(list)
      })
  },

  toggleCouponStatus(e) {
    const couponId = Number(e.currentTarget.dataset.id)
    const list = getLocalCoupons()
    const nextList = list.map((item) =>
      item.couponId === couponId
        ? { ...item, status: item.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE' }
        : item
    )
    setLocalCoupons(nextList)
    this.renderCoupons(nextList)
    util.showToast('状态已更新', 'success')
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
        const list = getLocalPromotions()
        const maxId = list.reduce((max, item) => Math.max(max, item.promotionId || 0), 0)
        list.unshift({
          promotionId: maxId + 1,
          ...promoData,
          status: 'ACTIVE',
          createTime: Date.now()
        })
        setLocalPromotions(list)
        util.showToast('已本地创建满减活动', 'success')
        this.setData({ showPromotionForm: false })
        this.renderPromotions(list)
      })
  },

  togglePromotionStatus(e) {
    const promotionId = Number(e.currentTarget.dataset.id)
    const list = getLocalPromotions()
    const nextList = list.map((item) =>
      item.promotionId === promotionId
        ? { ...item, status: item.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE' }
        : item
    )
    setLocalPromotions(nextList)
    this.renderPromotions(nextList)
    util.showToast('状态已更新', 'success')
  }
})
