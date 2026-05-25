const util = require('../../utils/util')
const api = require('../../api/index')

const app = getApp()

Page({
  data: {
    canManageStore: false,
    form: {
      storeName: '',
      brandSlogan: '',
      notice: '',
      businessHours: '',
      phone: '',
      address: '',
      deliveryRange: '',
      deliveryFeeText: '',
      freeDeliveryAmountText: '',
      serviceTagsText: '',
      bannerTitlesText: '',
      businessStatus: true,
      autoAccept: false,
      supportRefund: true,
      supportBooking: true,
      stockAlertThreshold: '20'
    }
  },

  onShow() {
    if (!app.needLogin()) return
    const canManageStore = app.hasAnyPermission(['store.manage'])
    this.setData({ canManageStore })
    if (canManageStore) {
      this.loadData()
    }
  },

  loadData() {
    api
      .getMerchantProfile()
      .then((storeInfo = {}) => {
        this.renderStoreForm(storeInfo)
      })
      .catch(() => {
        util.showToast('加载失败，请重试')
      })
  },

  renderStoreForm(storeInfo = {}) {
    this.setData({
      form: {
        ...storeInfo,
        deliveryRange: storeInfo.deliveryRange != null ? String(storeInfo.deliveryRange) : '',
        deliveryFeeText: storeInfo.deliveryFee != null ? util.formatPrice(storeInfo.deliveryFee) : '',
        freeDeliveryAmountText: storeInfo.freeDeliveryAmount != null ? util.formatPrice(storeInfo.freeDeliveryAmount) : '',
        serviceTagsText: (storeInfo.serviceTags || []).join('、'),
        bannerTitlesText: (storeInfo.bannerTitles || []).join('、'),
        autoAccept: !!storeInfo.autoAccept,
        stockAlertThreshold: String(storeInfo.stockAlertThreshold || 20)
      }
    })
  },

  handleInput(e) {
    const key = e.currentTarget.dataset.key
    this.setData({
      [`form.${key}`]: e.detail.value
    })
  },

  handleSwitchChange(e) {
    const key = e.currentTarget.dataset.key
    this.setData({
      [`form.${key}`]: e.detail.value
    })
  },

  saveStore() {
    const form = this.data.form
    const storeInfo = {
      ...form,
      deliveryRange: Number(form.deliveryRange || 0),
      deliveryFee: Math.round(Number(form.deliveryFeeText || 0) * 100),
      freeDeliveryAmount: Math.round(Number(form.freeDeliveryAmountText || 0) * 100),
      stockAlertThreshold: Number(form.stockAlertThreshold || 20),
      serviceTags: (form.serviceTagsText || '')
        .split('、')
        .map((item) => item.trim())
        .filter(Boolean),
      bannerTitles: (form.bannerTitlesText || '')
        .split('、')
        .map((item) => item.trim())
        .filter(Boolean)
    }

    api
      .updateMerchantProfile(storeInfo)
      .then((savedStoreInfo) => {
        util.showToast('门店信息已保存', 'success')
        this.renderStoreForm(savedStoreInfo || storeInfo)
      })
      .catch(() => {
        util.showToast('保存失败，请重试')
      })
  }
})
