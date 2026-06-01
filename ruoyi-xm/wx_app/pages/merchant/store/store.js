const util = require('../../../utils/merchant-util')
const api = require('../../../api/merchant-mini/index')

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
      serviceTagsText: '',
      bannerTitlesText: '',
      businessStatus: true,
      supportRefund: true,
      supportBooking: true,
      stockAlertThreshold: '20'
    }
  },

  onShow() {
    if (!app.needMerchantLogin()) return
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
        util.setStoreInfo(storeInfo)
        this.renderStoreForm(storeInfo)
      })
      .catch(() => {
        this.renderStoreForm(util.getStoreInfo())
      })
  },

  renderStoreForm(storeInfo = {}) {
    this.setData({
      form: {
        ...storeInfo,
        serviceTagsText: (storeInfo.serviceTags || []).join('、'),
        bannerTitlesText: (storeInfo.bannerTitles || []).join('、'),
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
        util.setStoreInfo(savedStoreInfo || storeInfo)
        util.showToast('门店信息已保存', 'success')
        this.renderStoreForm(savedStoreInfo || storeInfo)
      })
      .catch(() => {
        util.setStoreInfo(storeInfo)
        util.showToast('后端未联通，已保存本地演示数据')
        this.renderStoreForm(storeInfo)
      })
  }
})
